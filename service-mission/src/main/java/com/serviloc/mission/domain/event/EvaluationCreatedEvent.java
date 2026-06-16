package com.serviloc.mission.domain.event;

public record EvaluationCreatedEvent(
        String targetId,
        String targetRole,
        double newRating) { }
