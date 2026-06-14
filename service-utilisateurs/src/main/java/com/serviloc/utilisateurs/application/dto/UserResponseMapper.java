package com.serviloc.utilisateurs.application.dto;

import com.serviloc.utilisateurs.domain.model.User;

import java.time.format.DateTimeFormatter;

/**
 * Convertit les entités domaine en DTOs de réponse conformes au contrat API v2.0.
 */
public final class UserResponseMapper {

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    // Format fallback pour LocalDateTime (sans offset)
    private static final DateTimeFormatter LOCAL_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+01:00'");

    private UserResponseMapper() {}

    // ─── User de base (tous les rôles) ────────────────────────────

    public static AuthDtos.UserResponse toUserResponse(User user) {
        return new AuthDtos.UserResponse(
                UserIdFormatter.formatUserId(user.getId()),
                user.getRole().name().toLowerCase(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                user.getAvatarInitial(),
                user.getStatus().name().toLowerCase(),
                formatDate(user)
        );
    }

    // ─── Client profile ───────────────────────────────────────────

    public static ProfileDtos.ClientProfileResponse toClientProfile(User user) {
        return new ProfileDtos.ClientProfileResponse(
                UserIdFormatter.formatUserId(user.getId()),
                "client",
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                user.getAvatarInitial(),
                user.getStatus().name().toLowerCase(),
                0.0,        // totalSpent — sera alimenté par Service Paiement en S3
                0,          // completedMissions — sera alimenté par Service Missions en S3
                null,       // pendingPayment — stub S1
                null,       // location — stub S1
                formatDate(user)
        );
    }

    // ─── Provider profile ─────────────────────────────────────────

    public static ProfileDtos.ProviderProfileResponse toProviderProfile(User user) {
        return new ProfileDtos.ProviderProfileResponse(
                UserIdFormatter.formatUserId(user.getId()),
                "provider",
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                user.getAvatarInitial(),
                user.getStatus().name().toLowerCase(),
                null,       // specialty — ProviderProfile JPA en S2
                0.0,        // rating
                0,          // completedMissions
                false,      // isAvailable
                0.0,        // hourlyRate
                null,       // serviceZone
                ProfileDtos.WeeklyAvailability.defaultSchedule(),
                0.0,        // monthlyEarnings
                java.util.List.of(),
                false,      // estCertifie
                formatDate(user)
        );
    }

    public static ProfileDtos.ProviderProfileResponse toProviderProfile(
            User user, com.serviloc.utilisateurs.domain.model.ProviderProfile profile) {

        ProfileDtos.ServiceZone serviceZone = profile.getServiceZoneCity() != null
                ? new ProfileDtos.ServiceZone(profile.getServiceZoneCity(), profile.getRadiusKm())
                : null;

        return new ProfileDtos.ProviderProfileResponse(
                UserIdFormatter.formatUserId(user.getId()),
                "provider",
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                user.getAvatarInitial(),
                user.getStatus().name().toLowerCase(),
                profile.getSpecialty(),
                profile.getRating(),
                profile.getCompletedMissions(),
                profile.isAvailable(),
                profile.getHourlyRate(),
                serviceZone,
                ProfileDtos.WeeklyAvailability.defaultSchedule(),
                profile.getMonthlyEarnings(),
                profile.getCertifications(),
                profile.isEstCertifie(),
                formatDate(user)
        );
    }

    // ─── Agent profile ────────────────────────────────────────────

    public static ProfileDtos.AgentProfileResponse toAgentProfile(User user,
                                                                  String agentCode,
                                                                  String department,
                                                                  int assignedLitigesCount) {
        return new ProfileDtos.AgentProfileResponse(
                UserIdFormatter.formatUserId(user.getId()),
                "agent",
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                user.getAvatarInitial(),
                user.getStatus().name().toLowerCase(),
                agentCode,
                department,
                assignedLitigesCount,
                formatDate(user)
        );
    }


    // ─── Helper ───────────────────────────────────────────────────

    private static String formatDate(User user) {
        if (user.getCreatedAt() == null) return null;
        return user.getCreatedAt().format(LOCAL_FORMATTER);
    }
}