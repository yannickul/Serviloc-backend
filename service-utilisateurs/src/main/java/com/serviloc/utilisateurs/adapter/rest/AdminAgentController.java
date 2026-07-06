package com.serviloc.utilisateurs.adapter.rest;

import com.serviloc.utilisateurs.application.dto.AdminDtos.*;
import com.serviloc.utilisateurs.application.dto.ProfileDtos.AgentProfileResponse;
import com.serviloc.utilisateurs.application.service.AdminAgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/agents")
@Tag(name = "Admin — Agents", description = "Gestion des agents service client")
public class AdminAgentController {

    private final AdminAgentService adminAgentService;

    public AdminAgentController(AdminAgentService adminAgentService) {
        this.adminAgentService = adminAgentService;
    }

    // ─── POST /admin/agents ───────────────────────────────────────

    @PostMapping
    @Operation(summary = "Créer un compte agent")
    public ResponseEntity<ApiResponse<AgentProfileResponse>> createAgent(
            @Valid @RequestBody CreateAgentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(adminAgentService.createAgent(request)));
    }

    // ─── GET /admin/agents ────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Liste paginée des agents")
    public ResponseEntity<ApiResponse<AgentListResponse>> getAgents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(adminAgentService.getAgents(page, limit)));
    }
    // ─── PATCH /admin/agents/:id/suspend ──────────────────────────

    @PatchMapping("/{id}/suspend")
    @Operation(summary = "Suspendre un agent")
    public ResponseEntity<ApiResponse<SuspendResponse>> suspendAgent(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String adminUserId,
            @Valid @RequestBody SuspendUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminAgentService.suspendAgent(id, request, UUID.fromString(adminUserId))));
    }

// ─── DELETE /admin/agents/:id ──────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer définitivement un agent")
    public ResponseEntity<ApiResponse<AgentDeletedResponse>> deleteAgent(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(adminAgentService.deleteAgent(id)));
    }

    // ─── GET /admin/agents/:id ────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un agent")
    public ResponseEntity<ApiResponse<AgentProfileResponse>> getAgent(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(adminAgentService.getAgent(id)));
    }
}