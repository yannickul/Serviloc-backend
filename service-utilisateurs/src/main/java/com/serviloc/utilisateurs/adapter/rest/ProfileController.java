package com.serviloc.utilisateurs.adapter.rest;

import com.serviloc.utilisateurs.application.dto.ProfileDtos.*;
import com.serviloc.utilisateurs.domain.exception.UserNotFoundException;
import com.serviloc.utilisateurs.domain.model.User;
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
    public ResponseEntity<ClientProfileResponse> getClientProfile(
            @AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        return ResponseEntity.ok(new ClientProfileResponse(
                user.getId(), user.getEmail(), user.getPhone(),
                0.0, 0, null, user.getCreatedAt()
        ));
    }

    @GetMapping("/provider/me")
    @Operation(summary = "Profil du prestataire connecté")
    public ResponseEntity<ProviderProfileResponse> getProviderProfile(
            @AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        return ResponseEntity.ok(new ProviderProfileResponse(
                user.getId(), user.getEmail(), user.getPhone(),
                null, 0.0, false, 0.0, null, 0.0, false, user.getCreatedAt()
        ));
    }
}