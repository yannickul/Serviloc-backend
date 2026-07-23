package com.serviloc.paiement.infrastructure.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class MobileMoneyClient {

    private static final Logger log = LoggerFactory.getLogger(MobileMoneyClient.class);

    private final CamPayProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MobileMoneyClient(CamPayProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public record PaymentRequest(
            String phoneNumber,
            double amount,
            String currency,
            String description,
            String externalId
    ) {}

    public record PaymentResult(
            boolean success,
            String externalRef,
            String status,
            String message
    ) {}

    // ─── Collect (encaissement) ───────────────────────────────────

    @CircuitBreaker(name = "mobileMoney", fallbackMethod = "paymentFallback")
    @Retry(name = "mobileMoney")
    public PaymentResult initiatePayment(PaymentRequest request) {
        log.info("[CAMPAY] Initiation collecte : phone={} amount={} XAF",
                request.phoneNumber(), request.amount());
        try {
            String token = getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Token " + token);

            Map<String, Object> body = Map.of(
                    "amount",             String.valueOf((int) request.amount()),
                    "currency",           "XAF",
                    "from",               request.phoneNumber(),
                    "description",        request.description(),
                    "external_reference", request.externalId()
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    properties.getBaseUrl() + "/collect/", entity, String.class);

            JsonNode json = objectMapper.readTree(response.getBody());

            if (!response.getStatusCode().is2xxSuccessful()) {
                String error = json.path("detail").asText("Erreur inconnue");
                throw new MobileMoneyException("CamPay erreur : " + error);
            }

            String reference = json.path("reference").asText();
            log.info("[CAMPAY] Transaction initiée : ref={} — polling statut...", reference);

            // Polling — vérifie toutes les 5s pendant max 90s
            return pollTransactionStatus(reference, token, 18, 5000);

        } catch (MobileMoneyException e) {
            throw e;
        } catch (Exception e) {
            log.error("[CAMPAY] Exception collecte : {}", e.getMessage());
            throw new MobileMoneyException("Erreur CamPay : " + e.getMessage());
        }
    }

// ─── Polling statut ───────────────────────────────────────────

    private PaymentResult pollTransactionStatus(String reference, String token,
                                                int maxAttempts, long intervalMs) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                Thread.sleep(intervalMs);

                ResponseEntity<String> response = restTemplate.exchange(
                        properties.getBaseUrl() + "/transaction/" + reference + "/",
                        HttpMethod.GET, entity, String.class);

                JsonNode json = objectMapper.readTree(response.getBody());
                String status = json.path("status").asText();

                log.info("[CAMPAY] Polling {}/{} : ref={} status={}", attempt, maxAttempts, reference, status);

                if ("SUCCESSFUL".equalsIgnoreCase(status)) {
                    log.info("[CAMPAY] Paiement confirmé : ref={}", reference);
                    return new PaymentResult(true, reference, "SUCCESSFUL", "Paiement confirmé");
                } else if ("FAILED".equalsIgnoreCase(status)) {
                    log.warn("[CAMPAY] Paiement échoué : ref={}", reference);
                    return new PaymentResult(false, reference, "FAILED", "Paiement refusé");
                }
                // PENDING → on continue le polling

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new MobileMoneyException("Polling interrompu");
            } catch (Exception e) {
                log.warn("[CAMPAY] Erreur polling attempt={} : {}", attempt, e.getMessage());
            }
        }

        // Timeout — max attempts atteint
        log.warn("[CAMPAY] Timeout polling : ref={}", reference);
        return new PaymentResult(false, reference, "TIMEOUT",
                "Délai de confirmation dépassé");
    }

    // ─── Disburse (reversement au prestataire) ────────────────────

    @CircuitBreaker(name = "mobileMoney", fallbackMethod = "refundFallback")
    @Retry(name = "mobileMoney")
    public PaymentResult initiateRefund(PaymentRequest request) {
        log.info("[CAMPAY] Initiation reversement : phone={} amount={} XAF",
                request.phoneNumber(), request.amount());
        try {
            String token = getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Token " + token);

            Map<String, Object> body = Map.of(
                    "amount",             String.valueOf((int) request.amount()),
                    "currency",           "XAF",
                    "to",                 request.phoneNumber(),
                    "description",        request.description(),
                    "external_reference", request.externalId()
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    properties.getBaseUrl() + "/disburse/", entity, String.class);

            JsonNode json = objectMapper.readTree(response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                String reference = json.path("reference").asText();
                log.info("[CAMPAY] Reversement réussi : ref={}", reference);
                return new PaymentResult(true, reference, "SUCCESSFUL", "Reversement confirmé");
            } else {
                String error = json.path("detail").asText("Erreur inconnue");
                throw new MobileMoneyException("CamPay disburse erreur : " + error);
            }

        } catch (MobileMoneyException e) {
            throw e;
        } catch (Exception e) {
            log.error("[CAMPAY] Exception reversement : {}", e.getMessage());
            throw new MobileMoneyException("Erreur CamPay disburse : " + e.getMessage());
        }
    }

    // ─── Vérification statut transaction ──────────────────────────

    public String getTransactionStatus(String reference) {
        try {
            String token = getAccessToken();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Token " + token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    properties.getBaseUrl() + "/transaction/" + reference + "/",
                    HttpMethod.GET, entity, String.class);

            JsonNode json = objectMapper.readTree(response.getBody());
            return json.path("status").asText("PENDING");

        } catch (Exception e) {
            log.error("[CAMPAY] Erreur vérification statut : {}", e.getMessage());
            return "FAILED";
        }
    }

    // ─── Authentification CamPay ──────────────────────────────────

    private String getAccessToken() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = Map.of(
                    "username", properties.getUsername(),
                    "password", properties.getPassword()
            );

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    properties.getBaseUrl() + "/token/", entity, String.class);

            JsonNode json = objectMapper.readTree(response.getBody());
            String token = json.path("token").asText();

            if (token.isBlank()) {
                throw new MobileMoneyException("Token CamPay vide — vérifier les credentials");
            }
            return token;

        } catch (MobileMoneyException e) {
            throw e;
        } catch (Exception e) {
            throw new MobileMoneyException("Erreur authentification CamPay : " + e.getMessage());
        }
    }

    // ─── Fallbacks circuit breaker ────────────────────────────────

    public PaymentResult paymentFallback(PaymentRequest request, Throwable t) {
        log.error("[CAMPAY] Circuit breaker ouvert : {}", t.getMessage());
        return new PaymentResult(false, null, "FAILED",
                "Service Mobile Money temporairement indisponible");
    }

    public PaymentResult refundFallback(PaymentRequest request, Throwable t) {
        log.error("[CAMPAY] Reversement fallback : {}", t.getMessage());
        return new PaymentResult(false, null, "FAILED",
                "Service Mobile Money temporairement indisponible");
    }

    public static class MobileMoneyException extends RuntimeException {
        public MobileMoneyException(String message) { super(message); }
    }
}