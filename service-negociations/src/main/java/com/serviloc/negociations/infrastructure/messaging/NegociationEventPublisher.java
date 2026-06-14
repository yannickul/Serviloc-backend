package com.serviloc.negociations.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class NegociationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NegociationEventPublisher.class);
    private static final String EXCHANGE = "serviloc.events";

    private final RabbitTemplate rabbitTemplate;

    public NegociationEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // ─── negotiation.conversation_opened ─────────────────────────

    public void publishConversationOpened(UUID conversationId, UUID clientId,
                                          UUID providerId, UUID demandId) {
        publish("negotiation.conversation_opened", Map.of(
                "conversationId", conversationId.toString(),
                "clientId",       clientId.toString(),
                "providerId",     providerId.toString(),
                "demandId",       demandId.toString()
        ));
    }

    // ─── negotiation.message_sent ─────────────────────────────────

    public void publishMessageSent(UUID conversationId, UUID senderId,
                                   String senderRole, UUID clientId, UUID providerId) {
        publish("negotiation.message_sent", Map.of(
                "conversationId", conversationId.toString(),
                "senderId",       senderId.toString(),
                "senderRole",     senderRole,
                "clientId",       clientId.toString(),
                "providerId",     providerId.toString()
        ));
    }

    // ─── negotiation.quote_accepted ───────────────────────────────

    public void publishQuoteAccepted(UUID quoteId, UUID demandId,
                                     UUID clientId, UUID providerId,
                                     double amount, String paymentMethod,
                                     String phoneNumber) {
        publish("negotiation.quote.accepted", Map.of(
                "quoteId",       quoteId.toString(),
                "demandId",      demandId.toString(),
                "clientId",      clientId.toString(),
                "providerId",    providerId.toString(),
                "amount",        amount,
                "paymentMethod", paymentMethod,
                "phoneNumber",   phoneNumber
        ));
    }

    // ─── negotiation.quote_refused ────────────────────────────────

    public void publishQuoteRefused(UUID quoteId, UUID demandId, UUID providerId) {
        publish("negotiation.quote.refused", Map.of(
                "quoteId",    quoteId.toString(),
                "demandId",   demandId.toString(),
                "providerId", providerId.toString()
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