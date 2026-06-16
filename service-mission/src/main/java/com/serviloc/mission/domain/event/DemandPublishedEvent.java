package com.serviloc.mission.domain.event;

public record DemandPublishedEvent(
        String demandId,
        double lat,
        double lng,
        String categoryId,
        String clientId
) { }
