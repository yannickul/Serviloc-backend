// NegociationUpdateQuoteRequest.java
package com.serviloc.mission.infrastructure.external;

import java.math.BigDecimal;
import java.util.List;

public record NegociationUpdateQuoteRequest(
        String requestingProviderId,
        BigDecimal amount,
        String description,
        List<MaterialInputDto> materials,
        Integer estimatedDurationHours,
        Integer validityDays
) {}