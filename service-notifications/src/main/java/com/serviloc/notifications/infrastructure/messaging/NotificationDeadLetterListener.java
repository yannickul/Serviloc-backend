package com.serviloc.notifications.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Consumer de la dead-letter queue {@code notifications.dlq}.
 *
 * Un message arrive ici après épuisement des 3 tentatives de retry (cf. {@code RabbitMqConfig} et
 * {@code spring.rabbitmq.listener.simple.retry} dans application.yml). On loggue une alerte —
 * une supervision (alerting externe, ex: Sentry/Slack) pourra être branchée sur ce log plus tard.
 *
 * Le routing key d'origine n'est pas directement le routing key reçu sur la DLQ (RabbitMQ
 * republie avec {@code x-dead-letter-routing-key}) : on le retrouve dans le header standard
 * {@code x-death}, peuplé automatiquement par le broker.
 */
@Component
public class NotificationDeadLetterListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationDeadLetterListener.class);

    @RabbitListener(queues = "${serviloc.messaging.dlq}")
    @SuppressWarnings("unchecked")
    public void onDeadLetter(Message message) {
        Object xDeath = message.getMessageProperties().getHeaders().get("x-death");
        String originalRoutingKey = "inconnu";
        if (xDeath instanceof List<?> deaths && !deaths.isEmpty() && deaths.get(0) instanceof Map<?, ?> firstDeath) {
            Object routingKeys = firstDeath.get("routing-keys");
            if (routingKeys instanceof List<?> keys && !keys.isEmpty()) {
                originalRoutingKey = String.valueOf(keys.get(0));
            }
        }

        log.error("[ALERTE][DLQ] Message définitivement perdu après 3 tentatives — routingKey={} body={}",
                originalRoutingKey, new String(message.getBody()));
    }
}
