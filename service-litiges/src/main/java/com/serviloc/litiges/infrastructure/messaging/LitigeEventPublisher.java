// infrastructure/messaging/LitigeEventPublisher.java
package com.serviloc.litiges.infrastructure.messaging;

import com.serviloc.litiges.domain.event.LitigeOpenedEvent;
import com.serviloc.litiges.domain.event.LitigeResolvedEvent;
import com.serviloc.litiges.infrastructure.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LitigeEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishLitigeOpened(LitigeOpenedEvent event) {
        log.info("[EVENT] Émission litige.opened — litigeId={} reference={}",
                event.litigeId(), event.reference());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "litige.opened", event);
    }

    public void publishLitigeResolved(LitigeResolvedEvent event) {
        log.info("[EVENT] Émission litige.resolved — litigeId={} resolution={}",
                event.litigeId(), event.resolution());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "litige.resolved", event);
    }
}