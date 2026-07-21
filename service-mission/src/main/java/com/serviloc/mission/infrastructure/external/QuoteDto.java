// QuoteDto.java — réécrit entièrement selon le nouveau schéma de Baros
package com.serviloc.mission.infrastructure.external;

import java.math.BigDecimal;
import java.util.List;

public record QuoteDto(
        String id,
        String demandId,
        String providerId,
        String clientId,
        String laborDescription,
        BigDecimal laborAmount,
        List<MaterialResponseDto> materials,
        BigDecimal materialsTotal,
        BigDecimal totalAmount,
        Integer estimatedDurationHours,
        Integer validityDays,
        String status,
        String createdAt,
        String expiresAt
) {}