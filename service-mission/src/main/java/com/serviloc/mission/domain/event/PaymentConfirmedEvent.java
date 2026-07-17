package com.serviloc.mission.domain.event;

import java.math.BigDecimal;

public record PaymentConfirmedEvent(
        String transactionId,
        String demandId,
        String clientId,
        String providerId,
        BigDecimal amount,
        String externalRef
) {}