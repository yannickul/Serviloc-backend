package com.serviloc.paiement.infrastructure.external;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Client Mobile Money — Sandbox S3.
 * Simule Orange Money / MTN MoMo.
 * En S4 : remplacer par l'appel API réelle.
 */
@Component
public class MobileMoneyClient {

    private static final Logger log = LoggerFactory.getLogger(MobileMoneyClient.class);

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

    /**
     * Initie un paiement Mobile Money (sandbox).
     * Simule un succès à 90% — échec à 10% pour tester la compensation.
     */
    @CircuitBreaker(name = "mobileMoney", fallbackMethod = "paymentFallback")
    @Retry(name = "mobileMoney")
    public PaymentResult initiatePayment(PaymentRequest request) {
        log.info("[MOBILE MONEY] Initiation paiement : phone={} amount={} XAF",
                request.phoneNumber(), request.amount());

        // Sandbox : simulation
        simulateNetworkDelay();

        // Simulation échec si numéro de test spécial
        if (request.phoneNumber().endsWith("0000")) {
            log.warn("[MOBILE MONEY SANDBOX] Échec simulé pour numéro test : {}",
                    request.phoneNumber());
            throw new MobileMoneyException("Paiement refusé par l'opérateur (sandbox)");
        }

        String externalRef = "MM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("[MOBILE MONEY SANDBOX] Succès : externalRef={}", externalRef);

        return new PaymentResult(true, externalRef, "SUCCESSFUL",
                "Paiement confirmé (sandbox)");
    }

    /**
     * Remboursement Mobile Money (sandbox).
     */
    @CircuitBreaker(name = "mobileMoney", fallbackMethod = "refundFallback")
    @Retry(name = "mobileMoney")
    public PaymentResult initiateRefund(PaymentRequest request) {
        log.info("[MOBILE MONEY] Remboursement : phone={} amount={} XAF",
                request.phoneNumber(), request.amount());

        simulateNetworkDelay();

        String externalRef = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("[MOBILE MONEY SANDBOX] Remboursement OK : externalRef={}", externalRef);

        return new PaymentResult(true, externalRef, "REFUNDED",
                "Remboursement confirmé (sandbox)");
    }

    // ─── Fallbacks circuit breaker ────────────────────────────────

    public PaymentResult paymentFallback(PaymentRequest request, Throwable t) {
        log.error("[MOBILE MONEY] Circuit breaker ouvert ou timeout : {}", t.getMessage());
        return new PaymentResult(false, null, "FAILED",
                "Service Mobile Money temporairement indisponible");
    }

    public PaymentResult refundFallback(PaymentRequest request, Throwable t) {
        log.error("[MOBILE MONEY] Remboursement fallback : {}", t.getMessage());
        return new PaymentResult(false, null, "FAILED",
                "Service Mobile Money temporairement indisponible");
    }

    private void simulateNetworkDelay() {
        try {
            // Simule 500ms-1500ms de latence réseau
            Thread.sleep(500 + (long)(Math.random() * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static class MobileMoneyException extends RuntimeException {
        public MobileMoneyException(String message) { super(message); }
    }
}