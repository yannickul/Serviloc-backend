package com.serviloc.utilisateurs.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public final class ProfileDtos {

    public record ClientProfileResponse(
            UUID userId,
            String email,
            String phone,
            double totalSpent,
            int completedMissions,
            String location,
            LocalDateTime createdAt
    ) {}

    public record ProviderProfileResponse(
            UUID userId,
            String email,
            String phone,
            String specialty,
            double rating,
            boolean isAvailable,
            double hourlyRate,
            String serviceZone,
            double radiusKm,
            boolean estCertifie,
            LocalDateTime createdAt
    ) {}

    public record ProviderSummary(
            UUID providerId,
            String email,
            String phone,
            String specialty,
            double rating,
            double hourlyRate,
            String serviceZone,
            double radiusKm,
            boolean isAvailable
    ) {}
}