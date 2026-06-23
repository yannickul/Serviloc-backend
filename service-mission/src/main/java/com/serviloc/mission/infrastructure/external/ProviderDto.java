// ProviderDto.java
package com.serviloc.mission.infrastructure.external;

public record ProviderDto(
        String id,
        String fullName,
        String specialty,
        int radiusKm,
        double rating,
        int hourlyRate,
        double distanceKm,
        boolean isAvailable,
        int completedMissions
) {}