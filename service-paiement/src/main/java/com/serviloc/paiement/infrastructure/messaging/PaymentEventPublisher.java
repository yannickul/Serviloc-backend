package com.serviloc.paiement.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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

    public void publishPaymentReleased(UUID transactionId, UUID providerId,
                                       double netAmount, double commissionAmount) {
        publish("payment.released", Map.of(
                "transactionId",    transactionId.toString(),
                "providerId",       providerId.toString(),
                "netAmount",        netAmount,
                "commissionAmount", commissionAmount
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