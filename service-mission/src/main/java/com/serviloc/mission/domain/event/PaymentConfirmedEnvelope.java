// domain/event/PaymentConfirmedEnvelope.java
package com.serviloc.mission.domain.event;

/**
 * service-paiement enveloppe tous ses events sous la forme
 * {eventId, eventType, occurredAt, payload}. Ce record représente
 * cette enveloppe pour l'event payment.confirmed — le contenu métier
 * réel est dans payload().
 */
public record PaymentConfirmedEnvelope(
        String eventId,
        String eventType,
        String occurredAt,
        PaymentConfirmedEvent payload
) {}
