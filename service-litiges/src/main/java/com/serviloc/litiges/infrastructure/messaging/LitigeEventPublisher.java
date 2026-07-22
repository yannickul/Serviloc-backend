// infrastructure/messaging/LitigeEventPublisher.java
package com.serviloc.litiges.infrastructure.messaging;

import com.serviloc.litiges.domain.event.LitigeAssignedEvent;
import com.serviloc.litiges.domain.event.LitigeOpenedEvent;
import com.serviloc.litiges.domain.event.LitigeResolutionProposedEvent;
import com.serviloc.litiges.domain.event.LitigeResolvedEvent;
import com.serviloc.litiges.domain.model.ResolutionType;
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

    public void publishLitigeAssigned(LitigeAssignedEvent event) {
        log.info("[EVENT] Émission litige.assigned — litigeId={} agentId={}",
                event.litigeId(), event.agentId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "litige.assigned", event);
    }

    public void publishLitigeResolutionProposed(LitigeResolutionProposedEvent event) {
        log.info("[EVENT] Émission litige.resolution_proposed — litigeId={} type={}",
                event.litigeId(), event.type());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "litige.resolution_proposed", event);
    }

    public void publishLitigeResolved(LitigeResolvedEvent event) {
        log.info("[EVENT] Émission litige.resolved — litigeId={} resolution={}",
                event.litigeId(), event.resolution());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "litige.resolved", event);
    }

    /**
     * Traduit le type de résolution interne vers le libellé attendu par Service
     * Notifications dans le payload litige.resolved ("refund" | "reject").
     */
    public static String toResolutionLabel(ResolutionType type) {
        return switch (type) {
            case REMBOURSEMENT_TOTAL, REMBOURSEMENT_PARTIEL -> "refund";
            case AUCUN_REMBOURSEMENT, REJET -> "reject";
        };
    }
}
