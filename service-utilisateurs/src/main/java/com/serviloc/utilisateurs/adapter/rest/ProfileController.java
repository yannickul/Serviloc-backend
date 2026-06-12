package com.serviloc.utilisateurs.adapter.rest;

import com.serviloc.utilisateurs.application.dto.ProfileDtos.*;
import com.serviloc.utilisateurs.application.dto.ProviderProfileDtos.*;
import com.serviloc.utilisateurs.application.dto.UserResponseMapper;
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

import java.util.UUID;

@RestController
@Tag(name = "Profils", description = "Consultation et mise à jour des profils")
public class ProfileController {

    private final UserRepository userRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final ProviderProfileService providerProfileService;

    public ProfileController(UserRepository userRepository,
                             ProviderProfileRepository providerProfileRepository,
                             ProviderProfileService providerProfileService) {
        this.userRepository = userRepository;
        this.providerProfileRepository = providerProfileRepository;
        this.providerProfileService = providerProfileService;
    }

    // ─── GET /client/me ───────────────────────────────────────────

    @GetMapping("/client/me")
    @Operation(summary = "Profil du client connecté")
    public ResponseEntity<ApiResponse<ClientProfileResponse>> getClientProfile(
            @AuthenticationPrincipal UserDetails principal) {
        User user = getUser(principal);
        if (user.getRole() != UserRole.CLIENT)
            throw new IllegalStateException("Accès réservé aux clients");
        return ResponseEntity.ok(ApiResponse.ok(UserResponseMapper.toClientProfile(user)));
    }

    // ─── GET /provider/me ─────────────────────────────────────────

    @GetMapping("/provider/me")
    @Operation(summary = "Profil du prestataire connecté")
    public ResponseEntity<ApiResponse<ProviderProfileResponse>> getProviderProfile(
            @AuthenticationPrincipal UserDetails principal) {
        User user = getUser(principal);
        if (user.getRole() != UserRole.PROVIDER)
            throw new IllegalStateException("Accès réservé aux prestataires");

        return providerProfileRepository.findByUserId(user.getId())
                .map(profile -> ResponseEntity.ok(
                        ApiResponse.ok(UserResponseMapper.toProviderProfile(user, profile))))
                .orElse(ResponseEntity.ok(
                        ApiResponse.ok(UserResponseMapper.toProviderProfile(user))));
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

        ProfileUpdatedResponse response =
                providerProfileService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
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

        AvailabilityUpdatedResponse response =
                providerProfileService.updateAvailability(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
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

        ScheduleUpdatedResponse response =
                providerProfileService.updateSchedule(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ─── Helper ───────────────────────────────────────────────────

    private User getUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));
    }
}