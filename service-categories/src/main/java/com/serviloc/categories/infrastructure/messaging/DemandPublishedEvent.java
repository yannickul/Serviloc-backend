package com.serviloc.categories.infrastructure.messaging;

/**
 * Payload de l'événement RabbitMQ "demand.published" émis par Service Missions
 * (voir ARCHITECTURE_MICROSERVICES_SERVILOC.md ligne 725) :
 * { demandId, location, categoryId, clientId }
 * Seul categoryId est utilisé par service-categories.
 */
public record DemandPublishedEvent(
        String demandId,
        Object location,
        String categoryId,
        String clientId
) {
}
