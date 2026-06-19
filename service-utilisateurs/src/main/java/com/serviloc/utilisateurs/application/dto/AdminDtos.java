package com.serviloc.utilisateurs.application.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public final class AdminDtos {

    // ─── Agent Requests ───────────────────────────────────────────

    public record CreateAgentRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank @Email String email,
            @NotBlank @Pattern(regexp = "^\\+?[0-9]{9,15}$") String phone,
            @NotBlank String department
    ) {}

    public record SuspendUserRequest(
            @NotBlank String reason,
            @NotBlank @Pattern(regexp = "24h|7d|indefinite",
                    message = "Durée : 24h, 7d ou indefinite") String duration
    ) {}

    public record RejectProviderRequest(
            @NotBlank String reason
    ) {}

    public record NotifyProviderRequest(
            @NotBlank String message
    ) {}

    // ─── Agent Responses ──────────────────────────────────────────

    public record AgentListResponse(
            List<ProfileDtos.AgentProfileResponse> agents,
            PageMeta meta
    ) {}

    public record UserListResponse(
            List<AuthDtos.UserResponse> users,
            PageMeta meta
    ) {}

    public record ProviderListResponse(
            List<ProfileDtos.ProviderProfileResponse> providers,
            PageMeta meta
    ) {}

    public record PageMeta(
            int page,
            int limit,
            long total,
            int totalPages
    ) {}

    public record SuspendResponse(
            String userId,
            String status,
            String duration,
            String reason
    ) {}

    public record ProviderActionResponse(
            String providerId,
            String status,
            String message
    ) {}

    // ─── Agent Review Requests ────────────────────────────────────

    public record ProviderReviewRequest(
            @NotBlank
            @Pattern(regexp = "needs_revision|approved|rejected",
                    message = "verdict doit être needs_revision, approved ou rejected")
            String verdict,
            @NotBlank String comment
    ) {}

    // ─── Agent Review Response ────────────────────────────────────

    public record ProviderReviewResponse(
            String id,
            String agentId,
            String providerId,
            String verdict,
            String comment,
            String reviewedAt,
            String message
    ) {}

    public record AgentDeletedResponse(
            String agentId,
            boolean deleted
    ) {}
}