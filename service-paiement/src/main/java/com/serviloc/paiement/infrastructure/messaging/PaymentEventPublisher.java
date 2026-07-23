package com.serviloc.paiement.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);
    private static final String EXCHANGE = "serviloc.events";

    private final RabbitTemplate rabbitTemplate;

    public PaymentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // ─── payment.confirmed ────────────────────────────────────────

    public void publishPaymentConfirmed(UUID transactionId, UUID demandId,
                                        UUID clientId, UUID providerId,
                                        double amount, String externalRef) {
        publish("payment.confirmed", Map.of(
                "transactionId", transactionId.toString(),
                "demandId",      demandId.toString(),
                "clientId",      clientId.toString(),
                "providerId",    providerId.toString(),
                "amount",        amount,
                "externalRef",   externalRef
        ));
    }

    // ─── payment.failed ───────────────────────────────────────────

    public void publishPaymentFailed(UUID transactionId, UUID demandId,
                                     UUID clientId, String reason) {
        publish("payment.failed", Map.of(
                "transactionId", transactionId.toString(),
                "demandId",      demandId.toString(),
                "clientId",      clientId.toString(),
                "reason",        reason
        ));
    }

    // ─── payment.released ─────────────────────────────────────────

    public void publishPaymentReleased(UUID transactionId, UUID missionId, UUID providerId,
                                       double netAmount, double commissionAmount) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("transactionId",    transactionId.toString());
        payload.put("missionId",        missionId != null ? missionId.toString() : null);
        payload.put("providerId",       providerId.toString());
        payload.put("netAmount",        netAmount);
        payload.put("commissionAmount", commissionAmount);
        publish("payment.released", payload);
    }

    // ─── payment.refunded ─────────────────────────────────────────

    public void publishPaymentRefunded(UUID transactionId, UUID clientId, double amount) {
        publish("payment.refunded", Map.of(
                "transactionId", transactionId.toString(),
                "clientId",      clientId.toString(),
                "amount",        amount
        ));
    }

    // ─── Helper ───────────────────────────────────────────────────

    private void publish(String routingKey, Map<String, Object> payload) {
        Map<String, Object> event = Map.of(
                "eventId",    UUID.randomUUID().toString(),
                "eventType",  routingKey,
                "occurredAt", LocalDateTime.now().toString(),
                "payload",    payload
        );
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, routingKey, event);
            log.info("[RabbitMQ] Publié → routingKey={}", routingKey);
        } catch (Exception e) {
            log.error("[RabbitMQ] Échec publication routingKey={} : {}",
                    routingKey, e.getMessage());
        }
    }
}
