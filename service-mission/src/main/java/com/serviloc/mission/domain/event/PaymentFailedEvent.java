// domain/event/PaymentFailedEvent.java
package com.serviloc.mission.domain.event;

public record PaymentFailedEvent(
        String paymentId,
        String demandId,
        String reason
) {}