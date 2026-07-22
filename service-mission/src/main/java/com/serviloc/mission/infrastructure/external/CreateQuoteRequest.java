package com.serviloc.mission.infrastructure.external;

import java.math.BigDecimal;

public record CreateQuoteRequest(
        String demandId,
        String providerId,
        BigDecimal totalAmount,
        String paymentMethod,
        String phoneNumber
) {}