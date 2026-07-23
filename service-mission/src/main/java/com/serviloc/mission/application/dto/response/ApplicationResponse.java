// application/dto/response/ApplicationResponse.java
package com.serviloc.mission.application.dto.response;

import java.math.BigDecimal;

public record ApplicationResponse(
        String id,
        String demandId,
        String providerId,
        String providerName,
        String avatarInitial,
        String specialty,
        double rating,
        int missionsCompleted,
        double hourlyRate,
        BigDecimal estimatedTotal,
        String status,
        String appliedAt
) {}
