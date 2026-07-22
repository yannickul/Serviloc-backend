package com.serviloc.mission.domain.event;

public record MissionStartedEvent(
        String missionId,
        String clientId,
        String providerId) { }
