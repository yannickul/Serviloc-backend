// QuoteDto.java
package com.serviloc.mission.infrastructure.external;

import java.math.BigDecimal;

public record QuoteDto(
        String id,
        String demandId,
        String providerId,
        BigDecimal totalAmount,
        String paymentMethod,
        String phoneNumber,
        String status
) {}