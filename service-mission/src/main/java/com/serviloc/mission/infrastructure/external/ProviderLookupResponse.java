package com.serviloc.mission.infrastructure.external;

public record ProviderLookupResponse(
        String id,
        String fullName,
        String avatarInitial,
        String specialty,
        double rating,
        double hourlyRate,
        int completedMissions,
        boolean isAvailable
) {}
