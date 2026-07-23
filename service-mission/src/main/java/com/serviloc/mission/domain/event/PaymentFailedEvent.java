// domain/event/PaymentFailedEvent.java
package com.serviloc.mission.domain.event;

public record PaymentFailedEvent(
        String transactionId,
        String demandId,
        String reason
) {}
