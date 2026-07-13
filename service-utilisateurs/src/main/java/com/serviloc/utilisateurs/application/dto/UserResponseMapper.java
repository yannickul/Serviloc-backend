package com.serviloc.utilisateurs.application.dto;

import com.serviloc.utilisateurs.domain.model.User;

import java.time.format.DateTimeFormatter;
import com.serviloc.utilisateurs.application.service.ProfileEnrichmentService;
import java.util.List;
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
                user.getId().toString(),
                "provider",
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                user.getAvatarInitial(),
                null,       // avatarUrl
                user.getStatus().name().toLowerCase(),
                null, 0.0, 0, false, 0.0, null,
                ProfileDtos.WeeklyAvailability.defaultSchedule(),
                0.0,
                java.util.List.of(),
                false,
                java.util.List.of(),
                formatDate(user)
        );
    }

    public static ProfileDtos.ProviderProfileResponse toProviderProfile(
            User user,
            com.serviloc.utilisateurs.domain.model.ProviderProfile profile) {

        ProfileDtos.ServiceZone serviceZone = profile.getServiceZoneCity() != null
                ? new ProfileDtos.ServiceZone(profile.getServiceZoneCity(), profile.getRadiusKm())
                : null;

        List<ProfileDtos.ProviderDocument> documents = profile.getDocumentIds().stream()
                .map(docId -> new ProfileDtos.ProviderDocument(
                        docId, null, null, null, "valide", null))
                .toList();

        return new ProfileDtos.ProviderProfileResponse(
                user.getId().toString(),
                "provider",
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                user.getAvatarInitial(),
                profile.getAvatarUrl(),
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
                documents,
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
    // ─── ClientProfile enrichi ────────────────────────────────────

    public static ProfileDtos.ClientProfileResponse toClientProfile(
            User user,
            ProfileEnrichmentService.ClientEnrichment enrichment) {

        List<ProfileDtos.PendingPayment> pendingPayments = enrichment.pendingPayments()
                .stream()
                .map(p -> new ProfileDtos.PendingPayment(p.amount(), p.missionLabel()))
                .toList();

        return new ProfileDtos.ClientProfileResponse(
                user.getId().toString(),
                "client",
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                user.getAvatarInitial(),
                user.getStatus().name().toLowerCase(),
                enrichment.totalSpent(),
                enrichment.completedMissions(),
                pendingPayments,
                null,
                formatDate(user)
        );
    }

// ─── ProviderProfile enrichi ──────────────────────────────────

    public static ProfileDtos.ProviderProfileResponse toProviderProfile(
            User user,
            com.serviloc.utilisateurs.domain.model.ProviderProfile profile,
            ProfileEnrichmentService.ProviderEnrichment enrichment) {

        ProfileDtos.ServiceZone serviceZone = profile.getServiceZoneCity() != null
                ? new ProfileDtos.ServiceZone(profile.getServiceZoneCity(), profile.getRadiusKm())
                : null;

        List<ProfileDtos.ProviderDocument> documents = profile.getDocumentIds().stream()
                .map(docId -> new ProfileDtos.ProviderDocument(
                        docId, null, null, null, "valide", null))
                .toList();

        return new ProfileDtos.ProviderProfileResponse(
                user.getId().toString(),
                "provider",
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                user.getAvatarInitial(),
                profile.getAvatarUrl(),
                user.getStatus().name().toLowerCase(),
                profile.getSpecialty(),
                profile.getRating(),
                enrichment.completedMissions(),
                profile.isAvailable(),
                profile.getHourlyRate(),
                serviceZone,
                ProfileDtos.WeeklyAvailability.defaultSchedule(),
                enrichment.monthlyEarnings(),
                profile.getCertifications(),
                profile.isEstCertifie(),
                documents,
                formatDate(user)
        );
    }


    // ─── Helper ───────────────────────────────────────────────────

    private static String formatDate(User user) {
        if (user.getCreatedAt() == null) return null;
        return user.getCreatedAt().format(LOCAL_FORMATTER);
    }
}