// infrastructure/messaging/dto/PaymentReleasedEvent.java — remplacement complet
package com.serviloc.litiges.infrastructure.messaging.dto;

import java.math.BigDecimal;

public record PaymentReleasedEvent(
        String transactionId,
        String providerId,
        BigDecimal netAmount,
        BigDecimal commissionAmount
) {}