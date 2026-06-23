// domain/event/LitigeOpenedEvent.java
package com.serviloc.litiges.domain.event;

import java.time.Instant;

public record LitigeOpenedEvent(
        String litigeId,
        String reference,
        String demandId,
        String missionId,
        String clientId,
        String providerId,
        String motifId,
        Instant createdAt
) {}