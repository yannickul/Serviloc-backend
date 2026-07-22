package com.serviloc.mission.domain.event;

public record EvaluationCreatedEvent(
        String missionId,
        String targetId,
        String targetRole,
        int newRating) { }
