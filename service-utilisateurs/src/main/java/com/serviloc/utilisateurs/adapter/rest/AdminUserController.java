package com.serviloc.utilisateurs.adapter.rest;

import com.serviloc.utilisateurs.application.dto.AdminDtos.*;
import com.serviloc.utilisateurs.application.dto.AuthDtos.UserResponse;
import com.serviloc.utilisateurs.application.dto.ProfileDtos.ProviderProfileResponse;
import com.serviloc.utilisateurs.application.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "Admin — Utilisateurs", description = "Gestion des utilisateurs et prestataires")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    // ─── GET /admin/users ─────────────────────────────────────────

    @GetMapping("/admin/users")
    @Operation(summary = "Liste paginée des utilisateurs")
    public ResponseEntity<ApiResponse<UserListResponse>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(
                ApiResponse.ok(adminUserService.getUsers(role, status, search, page, limit)));
    }

    // ─── PATCH /admin/users/:id/suspend ───────────────────────────

    @PatchMapping("/admin/users/{id}/suspend")
    @Operation(summary = "Suspension d'un utilisateur")
    public ResponseEntity<ApiResponse<SuspendResponse>> suspendUser(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String adminUserId,
            @Valid @RequestBody SuspendUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminUserService.suspendUser(id, request, UUID.fromString(adminUserId))));
    }

    // ─── PATCH /admin/users/:id/reactivate ────────────────────────

    @PatchMapping("/admin/users/{id}/reactivate")
    @Operation(summary = "Réactivation d'un utilisateur")
    public ResponseEntity<ApiResponse<UserResponse>> reactivateUser(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(adminUserService.reactivateUser(id)));
    }

    // ─── GET /admin/providers ─────────────────────────────────────

    @GetMapping("/admin/providers")
    @Operation(summary = "Liste des prestataires")
    public ResponseEntity<ApiResponse<ProviderListResponse>> getProviders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(
                ApiResponse.ok(adminUserService.getProviders(status, page, limit)));
    }

    // ─── GET /admin/providers/:id ─────────────────────────────────

    @GetMapping("/admin/providers/{id}")
    @Operation(summary = "Dossier complet d'un prestataire")
    public ResponseEntity<ApiResponse<ProviderProfileResponse>> getProvider(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(adminUserService.getProvider(id)));
    }

    // ─── POST /admin/providers/:id/validate ───────────────────────

    @PostMapping("/admin/providers/{id}/validate")
    @Operation(summary = "Validation du dossier prestataire")
    public ResponseEntity<ApiResponse<ProviderActionResponse>> validateProvider(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String adminUserId) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminUserService.validateProvider(id, UUID.fromString(adminUserId))));
    }

    // ─── POST /admin/providers/:id/reject ─────────────────────────

    @PostMapping("/admin/providers/{id}/reject")
    @Operation(summary = "Rejet du dossier prestataire")
    public ResponseEntity<ApiResponse<ProviderActionResponse>> rejectProvider(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String adminUserId,
            @Valid @RequestBody RejectProviderRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminUserService.rejectProvider(id, request, UUID.fromString(adminUserId))));
    }

    // ─── POST /admin/providers/:id/notify ─────────────────────────

    @PostMapping("/admin/providers/{id}/notify")
    @Operation(summary = "Envoyer une notification à un prestataire")
    public ResponseEntity<ApiResponse<ProviderActionResponse>> notifyProvider(
            @PathVariable UUID id,
            @Valid @RequestBody NotifyProviderRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok(adminUserService.notifyProvider(id, request)));
    }
}