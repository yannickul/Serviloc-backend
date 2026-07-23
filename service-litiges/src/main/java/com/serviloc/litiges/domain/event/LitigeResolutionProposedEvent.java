// domain/event/LitigeResolutionProposedEvent.java
package com.serviloc.litiges.domain.event;

import java.math.BigDecimal;

/** Routing key: litige.resolution_proposed — notifie client + prestataire d'une proposition à valider. */
public record LitigeResolutionProposedEvent(
        String litigeId,
        String agentId,
        String type,
        BigDecimal refundAmount
) {}
