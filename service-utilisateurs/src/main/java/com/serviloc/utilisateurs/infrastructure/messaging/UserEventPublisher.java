package com.serviloc.utilisateurs.infrastructure.messaging;

import com.serviloc.utilisateurs.infrastructure.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class UserEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(UserEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public UserEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // ─── user.registered ─────────────────────────────────────────

    public void publishUserRegistered(UUID userId, String email, String role) {
        publish(RabbitMQConfig.RK_USER_REGISTERED, Map.of(
                "userId", userId.toString(),
                "email",  email,
                "role",   role
        ));
    }

    // ─── provider.validated ───────────────────────────────────────

    public void publishProviderValidated(UUID providerId, String email) {
        publish(RabbitMQConfig.RK_PROVIDER_VALIDATED, Map.of(
                "providerId", providerId.toString(),
                "email",      email
        ));
    }

    // ─── provider.rejected ────────────────────────────────────────

    public void publishProviderRejected(UUID providerId, String reason) {
        publish(RabbitMQConfig.RK_PROVIDER_REJECTED, Map.of(
                "providerId", providerId.toString(),
                "reason",     reason
        ));
    }

    // ─── provider.profile_updated ─────────────────────────────────

    public void publishProviderProfileUpdated(UUID providerId, String email) {
        publish("provider.profile_updated", Map.of(
                "providerId", providerId.toString(),
                "email",      email
        ));
    }

    // ─── provider.notified ────────────────────────────────────────

    public void publishProviderNotified(UUID providerId, String email, String message) {
        publish("provider.notified", Map.of(
                "providerId", providerId.toString(),
                "email",      email,
                "message",    message
        ));
    }

    // ─── user.suspended ───────────────────────────────────────────

    public void publishUserSuspended(UUID userId, String email) {
        publish(RabbitMQConfig.RK_USER_SUSPENDED, Map.of(
                "userId", userId.toString(),
                "email",  email
        ));
    }

    // ─── user.reactivated ─────────────────────────────────────────

    public void publishUserReactivated(UUID userId, String email) {
        publish("user.reactivated", Map.of(
                "userId", userId.toString(),
                "email",  email
        ));
    }

    // ─── agent.created ────────────────────────────────────────────

    public void publishAgentCreated(UUID agentId, String email,
                                    String agentCode, String tempPassword) {
        publish("agent.created", Map.of(
                "agentId",      agentId.toString(),
                "email",        email,
                "agentCode",    agentCode,
                "tempPassword", tempPassword
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
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, event);
            log.info("[RabbitMQ] Publié → routingKey={}", routingKey);
        } catch (Exception e) {
            log.error("[RabbitMQ] Échec publication routingKey={} : {}",
                    routingKey, e.getMessage());
        }
    }
}