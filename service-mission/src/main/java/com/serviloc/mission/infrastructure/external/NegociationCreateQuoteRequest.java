// NegociationCreateQuoteRequest.java — body sortant, respecte l'asymétrie amount/description (pas laborAmount/laborDescription en entrée)
package com.serviloc.mission.infrastructure.external;

import java.math.BigDecimal;
import java.util.List;

public record NegociationCreateQuoteRequest(
        String demandId,
        String providerId,
        BigDecimal amount,
        String description,
        List<MaterialInputDto> materials,
        Integer estimatedDurationHours,
        Integer validityDays
) {}