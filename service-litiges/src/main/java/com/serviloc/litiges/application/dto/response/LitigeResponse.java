// application/dto/response/LitigeResponse.java
package com.serviloc.litiges.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record LitigeResponse(
        String id,
        String reference,
        String demandId,
        String missionId,
        String clientId,
        String providerId,
        String agentId,
        String motifId,
        String description,
        List<String> evidenceIds,
        BigDecimal amount,
        String status,
        ResolutionDto resolution,
        Instant createdAt,
        Instant updatedAt
) {}