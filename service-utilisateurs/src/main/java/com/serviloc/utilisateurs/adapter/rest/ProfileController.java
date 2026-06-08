package com.serviloc.utilisateurs.adapter.rest;

import com.serviloc.utilisateurs.application.dto.ProfileDtos.*;
import com.serviloc.utilisateurs.application.dto.UserResponseMapper;
import com.serviloc.utilisateurs.domain.exception.UserNotFoundException;
import com.serviloc.utilisateurs.domain.model.User;
import com.serviloc.utilisateurs.domain.model.UserRole;
import com.serviloc.utilisateurs.domain.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Profils", description = "Consultation du profil connecté")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/client/me")
    @Operation(summary = "Profil du client connecté")
    public ResponseEntity<ApiResponse<ClientProfileResponse>> getClientProfile(
            @AuthenticationPrincipal UserDetails principal) {

        User user = getUser(principal);

        if (user.getRole() != UserRole.CLIENT) {
            throw new IllegalStateException("Accès réservé aux clients");
        }

        return ResponseEntity.ok(
                ApiResponse.ok(UserResponseMapper.toClientProfile(user))
        );
    }

    @GetMapping("/provider/me")
    @Operation(summary = "Profil du prestataire connecté")
    public ResponseEntity<ApiResponse<ProviderProfileResponse>> getProviderProfile(
            @AuthenticationPrincipal UserDetails principal) {

        User user = getUser(principal);

        if (user.getRole() != UserRole.PROVIDER) {
            throw new IllegalStateException("Accès réservé aux prestataires");
        }

        return ResponseEntity.ok(
                ApiResponse.ok(UserResponseMapper.toProviderProfile(user))
        );
    }

    // ─── Helper ───────────────────────────────────────────────────

    private User getUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));
    }
}