// domain/event/RatingUpdatePendingEvent.java
package com.serviloc.mission.domain.event;

public record RatingUpdatePendingEvent(
        String targetId,
        double newRating,
        int totalEvaluations
) {}