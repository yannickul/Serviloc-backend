package com.serviloc.utilisateurs.adapter.rest;

import com.serviloc.utilisateurs.infrastructure.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Contrôleur de test RabbitMQ — DEV UNIQUEMENT.
 * À supprimer avant la mise en production.
 */
@RestController
@RequestMapping("/test")
public class TestMessagingController {

    private static final Logger log = LoggerFactory.getLogger(TestMessagingController.class);

    private final RabbitTemplate rabbitTemplate;

    public TestMessagingController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // ─── Publisher ────────────────────────────────────────────────

    @PostMapping("/publish")
    public ResponseEntity<Map<String, String>> publish(
            @RequestParam String routingKey,
            @RequestBody(required = false) Map<String, Object> body) {

        Map<String, Object> payload = body != null ? body : Map.of(
                "eventId",    UUID.randomUUID().toString(),
                "eventType",  routingKey,
                "occurredAt", LocalDateTime.now().toString(),
                "payload",    Map.of("test", true)
        );

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, payload);
        log.info("[TEST] Publié → exchange={} routingKey={}", RabbitMQConfig.EXCHANGE, routingKey);

        return ResponseEntity.ok(Map.of(
                "status",     "published",
                "exchange",   RabbitMQConfig.EXCHANGE,
                "routingKey", routingKey
        ));
    }

    // ─── Consumer de test — écoute notifications.queue ───────────

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATIONS)
    public void consumeNotification(Map<String, Object> message) {
        log.info("[TEST-CONSUMER] notifications.queue ← {}", message);
    }
}