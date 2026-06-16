package com.serviloc.utilisateurs.adapter.rest;

import com.serviloc.utilisateurs.application.dto.AdminDtos.*;
import com.serviloc.utilisateurs.application.dto.ProfileDtos.ProviderProfileResponse;
import com.serviloc.utilisateurs.application.service.AgentProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/agent")
@Tag(name = "Agent — Prestataires", description = "Instruction des dossiers prestataires (UC30)")
public class AgentProviderController {

    private final AgentProviderService agentProviderService;

    public AgentProviderController(AgentProviderService agentProviderService) {
        this.agentProviderService = agentProviderService;
    }

    // ─── GET /agent/providers ─────────────────────────────────────

    @GetMapping("/providers")
    @Operation(summary = "Liste des prestataires en attente d'instruction")
    public ResponseEntity<ApiResponse<ProviderListResponse>> getPendingProviders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(
                ApiResponse.ok(agentProviderService.getPendingProviders(page, limit)));
    }

    // ─── GET /agent/providers/:id ─────────────────────────────────

    @GetMapping("/providers/{id}")
    @Operation(summary = "Dossier complet d'un prestataire")
    public ResponseEntity<ApiResponse<ProviderProfileResponse>> getProvider(
            @PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.ok(agentProviderService.getProvider(id)));
    }

    // ─── POST /agent/providers/:id/review ─────────────────────────

    @PostMapping("/providers/{id}/review")
    @Operation(summary = "Soumettre une instruction de dossier (UC30-agent)")
    public ResponseEntity<ApiResponse<ProviderReviewResponse>> submitReview(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String agentUserId,
            @Valid @RequestBody ProviderReviewRequest request) {
        ProviderReviewResponse response = agentProviderService.submitReview(
                UUID.fromString(agentUserId), id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}