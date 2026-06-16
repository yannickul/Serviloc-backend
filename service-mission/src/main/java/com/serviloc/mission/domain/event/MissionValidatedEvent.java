package com.serviloc.mission.domain.event;

import java.math.BigDecimal;

public record MissionValidatedEvent(
        String missionId,
        String validatedBy
) { }
