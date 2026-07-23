package com.serviloc.paiement.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviloc.paiement.application.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public PaymentEventConsumer(PaymentService paymentService, ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "payment.queue")
    public void onEvent(byte[] rawBytes) {
        try {
            String rawMessage = new String(rawBytes, java.nio.charset.StandardCharsets.UTF_8);
            log.info("[CONSUMER] Message reçu : {}", rawMessage);

            Map<String, Object> event = objectMapper.readValue(
                    rawMessage, new com.fasterxml.jackson.core.type.TypeReference<>() {});

            if (event.containsKey("eventType")) {
                // Format enveloppe standard : { eventId, eventType, occurredAt, payload }
                String eventType = (String) event.get("eventType");
                log.info("[CONSUMER] eventType={}", eventType);

                Map<String, Object> payload = (Map<String, Object>) event.get("payload");

                if ("negotiation.quote.accepted".equals(eventType)) {
                    handleQuoteAccepted(payload);
                } else if ("mission.completed".equals(eventType)) {
                    handleMissionCompleted(payload);
                } else {
                    log.warn("[CONSUMER] Event ignoré : eventType={}", eventType);
                }
            } else if (event.containsKey("transactionId") && event.containsKey("missionId")) {
                // Compat : service-mission publie MissionCompletedEvent brut, sans enveloppe.
                // TODO : à retirer une fois service-mission enveloppé (eventId/eventType/payload).
                log.info("[CONSUMER] Event brut détecté (sans enveloppe), traité comme mission.completed");
                handleMissionCompleted(event);
            } else {
                log.warn("[CONSUMER] Event ignoré : format non reconnu, clés={}", event.keySet());
            }

        } catch (Exception e) {
            log.error("[CONSUMER] Erreur traitement : {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void handleQuoteAccepted(Map<String, Object> payload) {
        UUID quoteId    = UUID.fromString((String) payload.get("quoteId"));
        UUID demandId   = UUID.fromString((String) payload.get("demandId"));
        UUID clientId   = UUID.fromString((String) payload.get("clientId"));
        UUID providerId = UUID.fromString((String) payload.get("providerId"));
        double amount   = ((Number) payload.get("amount")).doubleValue();
        String paymentMethod = (String) payload.getOrDefault("paymentMethod", "orange_money");
        String phoneNumber   = (String) payload.getOrDefault("phoneNumber", "+237600000000");

        log.info("[PAYMENT] Traitement quote_accepted : quoteId={} amount={}", quoteId, amount);
        paymentService.processQuoteAccepted(quoteId, demandId, clientId, providerId,
                amount, paymentMethod, phoneNumber);
    }

    private void handleMissionCompleted(Map<String, Object> payload) {
        UUID transactionId = UUID.fromString((String) payload.get("transactionId"));
        Object missionIdRaw = payload.get("missionId");
        String missionId = missionIdRaw != null ? missionIdRaw.toString() : null;
        log.info("[PAYMENT] Traitement mission.completed : transactionId={} missionId={}",
                transactionId, missionId);
        paymentService.releaseFunds(transactionId, missionId);
    }
}
