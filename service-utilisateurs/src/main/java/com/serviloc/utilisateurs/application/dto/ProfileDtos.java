package com.serviloc.utilisateurs.application.dto;

import java.util.List;

public final class ProfileDtos {

    // ─── Location ─────────────────────────────────────────────────

    public record Location(
            String city,
            String district
    ) {}

    // ─── PendingPayment ───────────────────────────────────────────

    public record PendingPayment(
            double amount,
            String missionLabel
    ) {}

    // ─── ServiceZone ──────────────────────────────────────────────

    public record ServiceZone(
            String city,
            double radiusKm
    ) {}

    // ─── DaySchedule ──────────────────────────────────────────────

    public record DaySchedule(
            String start,       // "08:00" ou null
            String end,         // "18:00" ou null
            boolean available
    ) {}

    // ─── WeeklyAvailability ───────────────────────────────────────

    public record WeeklyAvailability(
            DaySchedule monday,
            DaySchedule tuesday,
            DaySchedule wednesday,
            DaySchedule thursday,
            DaySchedule friday,
            DaySchedule saturday,
            DaySchedule sunday
    ) {
        // Disponibilité par défaut (lun-ven 08h-18h, sam 08h-13h, dim fermé)
        public static WeeklyAvailability defaultSchedule() {
            DaySchedule weekday = new DaySchedule("08:00", "18:00", true);
            DaySchedule saturday = new DaySchedule("08:00", "13:00", true);
            DaySchedule sunday = new DaySchedule(null, null, false);
            return new WeeklyAvailability(
                    weekday, weekday, weekday, weekday, weekday, saturday, sunday
            );
        }
    }

    // ─── ClientProfileResponse ────────────────────────────────────

    public record ClientProfileResponse(
            String id,              // usr_abc123
            String role,            // "client"
            String firstName,
            String lastName,
            String fullName,
            String phone,
            String email,
            String avatarInitial,
            String status,
            double totalSpent,
            int completedMissions,
            PendingPayment pendingPayment,   // null si aucun
            Location location,               // null si non renseigné
            String createdAt
    ) {}

    // ─── ProviderProfileResponse ──────────────────────────────────

    public record ProviderProfileResponse(
            String id,              // usr_jcm456
            String role,            // "provider"
            String firstName,
            String lastName,
            String fullName,
            String phone,
            String email,
            String avatarInitial,
            String status,
            String specialty,
            double rating,
            int completedMissions,
            boolean isAvailable,
            double hourlyRate,
            ServiceZone serviceZone,
            WeeklyAvailability availability,
            double monthlyEarnings,
            List<String> certifications,
            boolean estCertifie,
            String createdAt
    ) {}

    // ─── AgentProfileResponse ─────────────────────────────────────

    public record AgentProfileResponse(
            String id,              // usr_agent01
            String role,            // "agent"
            String firstName,
            String lastName,
            String fullName,
            String phone,
            String email,
            String avatarInitial,
            String status,
            String agentCode,
            String department,
            int assignedLitigesCount,
            String createdAt
    ) {}

    // ─── ProviderSummary (GET /internal/providers) ────────────────

    public record ProviderSummary(
            String providerId,
            String firstName,
            String lastName,
            String fullName,
            String phone,
            String specialty,
            double rating,
            double hourlyRate,
            ServiceZone serviceZone,
            boolean isAvailable
    ) {}
}