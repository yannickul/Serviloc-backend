package com.serviloc.utilisateurs.adapter.rest;

import com.serviloc.utilisateurs.application.dto.ProfileDtos.*;
import com.serviloc.utilisateurs.application.dto.ProviderProfileDtos.*;
import com.serviloc.utilisateurs.application.dto.UserResponseMapper;
import com.serviloc.utilisateurs.application.service.ProfileEnrichmentService;
import com.serviloc.utilisateurs.application.service.ProviderProfileService;
import com.serviloc.utilisateurs.domain.exception.UserNotFoundException;
import com.serviloc.utilisateurs.domain.model.User;
import com.serviloc.utilisateurs.domain.model.UserRole;
import com.serviloc.utilisateurs.domain.repository.ProviderProfileRepository;
import com.serviloc.utilisateurs.domain.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Profils", description = "Consultation et mise à jour des profils")
public class ProfileController {

    private final UserRepository userRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final ProviderProfileService providerProfileService;
    private final ProfileEnrichmentService profileEnrichmentService;

    public ProfileController(UserRepository userRepository,
                             ProviderProfileRepository providerProfileRepository,
                             ProviderProfileService providerProfileService,
                             ProfileEnrichmentService profileEnrichmentService) {
        this.userRepository = userRepository;
        this.providerProfileRepository = providerProfileRepository;
        this.providerProfileService = providerProfileService;
        this.profileEnrichmentService = profileEnrichmentService;
    }

    // ─── GET /user/{id} ────────────────────────────────────────────
    // Endpoint public (authentification requise, tout rôle) — profil
    // public d'un utilisateur, différencié selon son rôle réel.

    @GetMapping("/user/{id}")
    @Operation(summary = "Informations publiques d'un utilisateur")
    public ResponseEntity<ApiResponse<?>> getPublicUser(@PathVariable java.util.UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable : " + id));

        if (user.getRole() == UserRole.PROVIDER) {
            Object body = providerProfileRepository.findByUserId(id)
                    .map(profile -> UserResponseMapper.toPublicProviderProfile(user, profile))
                    .orElseGet(() -> UserResponseMapper.toPublicProviderProfile(user));
            return ResponseEntity.ok(ApiResponse.ok(body));
        }

        return ResponseEntity.ok(ApiResponse.ok(UserResponseMapper.toPublicClientProfile(user)));
    }

    // ─── GET /client/me ───────────────────────────────────────────

    @GetMapping("/client/me")
    @Operation(summary = "Profil du client connecté")
    public ResponseEntity<ApiResponse<ClientProfileResponse>> getClientProfile(
            @AuthenticationPrincipal UserDetails principal) {
        User user = getUser(principal);
        if (user.getRole() != UserRole.CLIENT)
            throw new IllegalStateException("Accès réservé aux clients");

        ProfileEnrichmentService.ClientEnrichment enrichment =
                profileEnrichmentService.getClientEnrichment(user.getId());

        return ResponseEntity.ok(ApiResponse.ok(
                UserResponseMapper.toClientProfile(user, enrichment)));
    }

    // ─── PATCH /client/profile ─────────────────────────────────────

    @PatchMapping("/client/profile")
    @Operation(summary = "Mise à jour du profil client")
    public ResponseEntity<ApiResponse<com.serviloc.utilisateurs.application.dto.ClientProfileDtos.ClientProfileUpdatedResponse>> updateClientProfile(
            @AuthenticationPrincipal UserDetails principal,
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody
            com.serviloc.utilisateurs.application.dto.ClientProfileDtos.UpdateClientProfileRequest request) {
        User user = getUser(principal);
        if (user.getRole() != UserRole.CLIENT)
            throw new IllegalStateException("Accès réservé aux clients");

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        if (request.avatarUrl() != null && !request.avatarUrl().isBlank())
            user.setAvatarUrl(request.avatarUrl());

        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.ok(
                new com.serviloc.utilisateurs.application.dto.ClientProfileDtos.ClientProfileUpdatedResponse(
                        user.getId().toString(),
                        "Profil mis à jour avec succès")));
    }

    // ─── GET /provider/me ─────────────────────────────────────────

    @GetMapping("/provider/me")
    @Operation(summary = "Profil du prestataire connecté")
    public ResponseEntity<ApiResponse<ProviderProfileResponse>> getProviderProfile(
            @AuthenticationPrincipal UserDetails principal) {
        User user = getUser(principal);
        if (user.getRole() != UserRole.PROVIDER)
            throw new IllegalStateException("Accès réservé aux prestataires");

        ProfileEnrichmentService.ProviderEnrichment enrichment =
                profileEnrichmentService.getProviderEnrichment(user.getId());

        return providerProfileRepository.findByUserId(user.getId())
                .map(profile -> ResponseEntity.ok(ApiResponse.ok(
                        UserResponseMapper.toProviderProfile(user, profile, enrichment))))
                .orElse(ResponseEntity.ok(ApiResponse.ok(
                        UserResponseMapper.toProviderProfile(user))));
    }

    // ─── PATCH /provider/profile ──────────────────────────────────

    @PatchMapping("/provider/profile")
    @Operation(summary = "Mise à jour du profil prestataire (UC18)")
    public ResponseEntity<ApiResponse<ProfileUpdatedResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        User user = getUser(principal);
        if (user.getRole() != UserRole.PROVIDER)
            throw new IllegalStateException("Accès réservé aux prestataires");

        return ResponseEntity.ok(ApiResponse.ok(
                providerProfileService.updateProfile(user.getId(), request)));
    }

    // ─── PATCH /provider/availability ─────────────────────────────

    @PatchMapping("/provider/availability")
    @Operation(summary = "Mise à jour de la disponibilité du prestataire")
    public ResponseEntity<ApiResponse<AvailabilityUpdatedResponse>> updateAvailability(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdateAvailabilityRequest request) {
        User user = getUser(principal);
        if (user.getRole() != UserRole.PROVIDER)
            throw new IllegalStateException("Accès réservé aux prestataires");

        return ResponseEntity.ok(ApiResponse.ok(
                providerProfileService.updateAvailability(user.getId(), request)));
    }

    // ─── PATCH /provider/schedule ─────────────────────────────────

    @PatchMapping("/provider/schedule")
    @Operation(summary = "Mise à jour des horaires hebdomadaires du prestataire")
    public ResponseEntity<ApiResponse<ScheduleUpdatedResponse>> updateSchedule(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdateScheduleRequest request) {
        User user = getUser(principal);
        if (user.getRole() != UserRole.PROVIDER)
            throw new IllegalStateException("Accès réservé aux prestataires");

        return ResponseEntity.ok(ApiResponse.ok(
                providerProfileService.updateSchedule(user.getId(), request)));
    }

    // ─── Helper ───────────────────────────────────────────────────

    private User getUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));
    }
}