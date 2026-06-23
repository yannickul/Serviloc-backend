// domain/event/QuoteAcceptedEvent.java
package com.serviloc.mission.domain.event;

public record QuoteAcceptedEvent(
        String quoteId,
        String demandId,
        String clientId,
        String providerId,
        String paymentMethod,
        String phoneNumber
) {}