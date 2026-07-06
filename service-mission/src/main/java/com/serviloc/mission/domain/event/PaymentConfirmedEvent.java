// domain/event/PaymentConfirmedEvent.java
package com.serviloc.mission.domain.event;

public record PaymentConfirmedEvent(
        String paymentId,
        String demandId,
        String quoteId,
        String clientId,
        String providerId,
        java.math.BigDecimal totalAmount,
        java.math.BigDecimal sequesteredAmount,
        String category,
        int estimatedDurationHours
) {}