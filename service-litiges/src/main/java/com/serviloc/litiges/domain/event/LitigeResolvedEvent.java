// domain/event/LitigeResolvedEvent.java
package com.serviloc.litiges.domain.event;

import java.math.BigDecimal;

/**
 * Routing key: litige.resolved — consommé par Service Notifications
 * (push client + push prestataire "Litige résolu"). Format imposé, ne pas modifier
 * sans coordination avec l'équipe Notifications.
 */
public record LitigeResolvedEvent(
        String litigeId,
        String resolution,      // "refund" | "reject" — voir LitigeEventPublisher#toResolutionLabel
        BigDecimal refundAmount,
        String clientId,
        String providerId
) {}
