// domain/event/LitigeResolvedEvent.java
package com.serviloc.litiges.domain.event;

import java.math.BigDecimal;
import java.time.Instant;

public record LitigeResolvedEvent(
        String litigeId,
        String reference,
        String resolution,      // ResolutionType.name()
        BigDecimal refundAmount,
        String note,
        Instant resolvedAt
) {}