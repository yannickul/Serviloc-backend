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
            String demandId   // optionnel — ex. conversation d'urgence sans demande liée
    ) {}

    public record SendMessageRequest(
            @NotBlank String content,
            String imageId   // optionnel
    ) {}

    // ─── Responses ────────────────────────────────────────────────

    public record ConversationResponse(
            String id,
            String demandId,
            ClientSummary client,
            ProviderSummary provider,
            String status,
            int unreadCount,
            LastMessageSummary lastMessage,
            String createdAt,
            String updatedAt
    ) {}

    public record ClientSummary(
            String id,
            String fullName,
            String avatarInitial,
            boolean isOnline
    ) {}

    public record ProviderSummary(
            String id,
            String fullName,
            String avatarInitial,
            double rating,
            String specialty,
            boolean isOnline
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
            String imageUrl,
            boolean read,
            boolean deleted,
            String sentAt
    ) {}

    public record DeleteMessageResponse(
            String messageId,
            boolean deleted
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
            String clientId,
            String laborDescription,
            double laborAmount,
            List<MaterialResponse> materials,
            double materialsTotal,
            double totalAmount,
            int estimatedDurationHours,
            int validityDays,
            String status,
            String createdAt,
            String expiresAt
    ) {}

    public record MaterialResponse(
            String id,
            String name,
            int quantity,
            double unitPrice,
            double subtotal
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
            int estimatedDurationHours,
            @jakarta.validation.constraints.Min(value = 1, message = "validityDays doit être >= 1")
            int validityDays
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
            @NotNull String requestingProviderId,
            @Positive double amount,
            String description,
            List<MaterialRequest> materials,
            int estimatedDurationHours,
            int validityDays
    ) {}
}