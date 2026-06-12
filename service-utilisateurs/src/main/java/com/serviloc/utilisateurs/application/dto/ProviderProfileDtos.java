package com.serviloc.utilisateurs.application.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public final class ProviderProfileDtos {

    // ─── PATCH /provider/profile ──────────────────────────────────

    public record UpdateProfileRequest(
            String specialty,
            @Min(0) double hourlyRate,
            String serviceZoneCity,
            @Min(1) @Max(100) double radiusKm,
            boolean estCertifie,
            List<String> certifications,
            List<String> documentIds
    ) {}

    // ─── PATCH /provider/availability ─────────────────────────────

    public record UpdateAvailabilityRequest(
            boolean isAvailable
    ) {}

    // ─── PATCH /provider/schedule ─────────────────────────────────

    public record DayScheduleRequest(
            String start,
            String end,
            boolean available
    ) {}

    public record UpdateScheduleRequest(
            DayScheduleRequest monday,
            DayScheduleRequest tuesday,
            DayScheduleRequest wednesday,
            DayScheduleRequest thursday,
            DayScheduleRequest friday,
            DayScheduleRequest saturday,
            DayScheduleRequest sunday
    ) {}

    // ─── Responses ────────────────────────────────────────────────

    public record ProfileUpdatedResponse(
            String providerId,
            String message
    ) {}

    public record AvailabilityUpdatedResponse(
            String providerId,
            boolean isAvailable,
            String message
    ) {}

    public record ScheduleUpdatedResponse(
            String providerId,
            String message
    ) {}
}