package com.serviloc.mission.domain.event;

import java.math.BigDecimal;

public record MissionCompletedEvent(
        String missionId,
        String clientId,
        String providerId,
        BigDecimal amount
) { }
