package com.serviloc.negociations.adapter.rest;

import com.serviloc.negociations.application.dto.NegociationDtos.*;
import com.serviloc.negociations.application.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "Conversations", description = "Gestion des conversations et messages")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    // ─── POST /client/conversations ───────────────────────────────

    @PostMapping("/client/conversations")
    @Operation(summary = "Créer une conversation (idempotente)")
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateConversationRequest request) {
        ConversationResponse response = conversationService
                .createConversation(UUID.fromString(userId), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    // ─── GET /client/conversations ────────────────────────────────

    @GetMapping("/client/conversations")
    @Operation(summary = "Liste des conversations du client")
    public ResponseEntity<ApiResponse<ConversationListResponse>> getClientConversations(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(
                conversationService.getClientConversations(
                        UUID.fromString(userId), page, limit)));
    }

    // ─── GET /client/conversations/:id/messages ───────────────────

    @GetMapping("/client/conversations/{id}/messages")
    @Operation(summary = "Messages d'une conversation (client)")
    public ResponseEntity<ApiResponse<MessageListResponse>> getClientMessages(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(
                conversationService.getMessages(id, UUID.fromString(userId), page, limit)));
    }

    // ─── POST /client/conversations/:id/messages ──────────────────

    @PostMapping("/client/conversations/{id}/messages")
    @Operation(summary = "Envoyer un message (client)")
    public ResponseEntity<ApiResponse<MessageResponse>> sendClientMessage(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID id,
            @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        conversationService.sendMessage(
                                id, UUID.fromString(userId), "client", request)));
    }

    // ─── GET /provider/conversations ──────────────────────────────

    @GetMapping("/provider/conversations")
    @Operation(summary = "Liste des conversations du prestataire")
    public ResponseEntity<ApiResponse<ConversationListResponse>> getProviderConversations(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(
                conversationService.getProviderConversations(
                        UUID.fromString(userId), page, limit)));
    }

    // ─── GET /provider/conversations/:id/messages ─────────────────

    @GetMapping("/provider/conversations/{id}/messages")
    @Operation(summary = "Messages d'une conversation (prestataire)")
    public ResponseEntity<ApiResponse<MessageListResponse>> getProviderMessages(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(
                conversationService.getMessages(id, UUID.fromString(userId), page, limit)));
    }

    // ─── POST /provider/conversations/:id/messages ────────────────

    @PostMapping("/provider/conversations/{id}/messages")
    @Operation(summary = "Envoyer un message (prestataire)")
    public ResponseEntity<ApiResponse<MessageResponse>> sendProviderMessage(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID id,
            @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        conversationService.sendMessage(
                                id, UUID.fromString(userId), "provider", request)));
    }
}