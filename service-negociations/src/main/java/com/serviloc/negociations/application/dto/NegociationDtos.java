package com.serviloc.negociations.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public final class NegociationDtos {

    // ─── Requests ─────────────────────────────────────────────────

    public record CreateConversationRequest(
            @NotNull String providerId,
            @NotNull String demandId
    ) {}

    public record SendMessageRequest(
            @NotBlank String content,
            String imageId   // optionnel
    ) {}

    // ─── Responses ────────────────────────────────────────────────

    public record ConversationResponse(
            String id,
            String demandId,
            ParticipantSummary client,
            ParticipantSummary provider,
            String status,
            int unreadCount,
            LastMessageSummary lastMessage,
            String createdAt,
            String updatedAt
    ) {}

    public record ParticipantSummary(
            String id,
            String firstName,
            String lastName,
            String fullName,
            String avatarInitial
    ) {}

    public record LastMessageSummary(
            String content,
            String sentAt,
            String senderRole
    ) {}

    public record MessageResponse(
            String id,
            String conversationId,
            String senderId,
            String senderRole,
            String content,
            String imageId,
            boolean read,
            String sentAt
    ) {}

    public record ConversationListResponse(
            List<ConversationResponse> conversations,
            PageMeta meta
    ) {}

    public record MessageListResponse(
            List<MessageResponse> messages,
            PageMeta meta
    ) {}

    public record PageMeta(
            int page,
            int limit,
            long total,
            int totalPages
    ) {}

    // ─── Internal (inter-services) ────────────────────────────────

    public record QuoteResponse(
            String id,
            String demandId,
            String providerId,
            double amount,
            String description,
            String status,
            String expiresAt,
            String createdAt
    ) {}

    public record ConversationInternalResponse(
            String id,
            String demandId,
            String clientId,
            String providerId,
            String status,
            String createdAt
    ) {}

    // ─── Quote Requests ───────────────────────────────────────────

    public record CreateQuoteRequest(
            @NotNull String demandId,
            @NotNull String providerId,
            @Positive double amount,
            String description,
            List<MaterialRequest> materials,
            int estimatedDurationHours
    ) {}

    public record MaterialRequest(
            @NotBlank String name,
            @Positive int quantity,
            @PositiveOrZero double unitPrice
    ) {}

    public record UpdateQuoteStatusRequest(
            @NotBlank String status,          // accepte | refuse
            String paymentMethod,             // orange_money | mtn_momo
            String phoneNumber
    ) {}

    public record UpdateQuoteRequest(
            @Positive double amount,
            String description,
            List<MaterialRequest> materials,
            int estimatedDurationHours
    ) {}
}