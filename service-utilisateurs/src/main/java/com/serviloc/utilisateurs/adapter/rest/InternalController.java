package com.serviloc.utilisateurs.adapter.rest;

import com.serviloc.utilisateurs.application.dto.ProfileDtos.ProviderSummary;
import com.serviloc.utilisateurs.application.dto.UserIdFormatter;
import com.serviloc.utilisateurs.application.dto.UserResponseMapper;
import com.serviloc.utilisateurs.domain.exception.UserNotFoundException;
import com.serviloc.utilisateurs.domain.model.ProviderProfile;
import com.serviloc.utilisateurs.domain.model.User;
import com.serviloc.utilisateurs.domain.repository.ProviderProfileRepository;
import com.serviloc.utilisateurs.domain.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.serviloc.utilisateurs.domain.model.UserRole;
import com.serviloc.utilisateurs.infrastructure.persistence.UserJpaRepository;
import com.serviloc.utilisateurs.infrastructure.messaging.UserEventPublisher;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal")
@Tag(name = "Internal", description = "Endpoints inter-services — non exposés publiquement")
public class InternalController {

    private static final Logger log = LoggerFactory.getLogger(InternalController.class);

    private final UserRepository userRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final UserEventPublisher eventPublisher;
    private final UserJpaRepository userJpaRepository;

    public InternalController(UserRepository userRepository,
                              ProviderProfileRepository providerProfileRepository,
                              UserEventPublisher eventPublisher,
                              UserJpaRepository userJpaRepository) {
        this.userRepository = userRepository;
        this.providerProfileRepository = providerProfileRepository;
        this.eventPublisher = eventPublisher;
        this.userJpaRepository = userJpaRepository;
    }

    // ─── GET /internal/providers/top ───────────────────────────────

    @GetMapping("/providers/top")
    @Operation(summary = "Top prestataires par nombre de missions effectuées")
    public ResponseEntity<List<com.serviloc.utilisateurs.application.dto.ProfileDtos.TopProviderResponse>> getTopProviders(
            @RequestParam(defaultValue = "10") int limit) {

        List<ProviderProfile> topProfiles = providerProfileRepository.findTopByCompletedMissions(limit);

        List<com.serviloc.utilisateurs.application.dto.ProfileDtos.TopProviderResponse> result = topProfiles.stream()
                .map(profile -> {
                    String fullName = userRepository.findById(profile.getUserId())
                            .map(User::getFullName)
                            .orElse("Utilisateur inconnu");
                    return new com.serviloc.utilisateurs.application.dto.ProfileDtos.TopProviderResponse(
                            profile.getUserId().toString(),
                            fullName,
                            profile.getCompletedMissions(),
                            profile.getRating()
                    );
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    // ─── GET /internal/providers/{id} ───────────────────────────────
    // Demandé par Service Missions : lookup d'un prestataire précis par id
    // (GET /internal/providers ne permet qu'une recherche géo-filtrée).

    @GetMapping("/providers/{id}")
    @Operation(summary = "Profil prestataire par id (lookup précis, hors recherche géo)")
    public ResponseEntity<com.serviloc.utilisateurs.application.dto.ProfileDtos.ProviderLookupResponse> getProviderById(
            @PathVariable UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Prestataire introuvable : " + id));

        if (user.getRole() != UserRole.PROVIDER) {
            throw new UserNotFoundException("Prestataire introuvable : " + id);
        }

        ProviderProfile profile = providerProfileRepository.findByUserId(id)
                .orElseThrow(() -> new UserNotFoundException("Prestataire introuvable : " + id));

        var response = new com.serviloc.utilisateurs.application.dto.ProfileDtos.ProviderLookupResponse(
                UserIdFormatter.formatUserId(user.getId()),
                user.getFullName(),
                user.getAvatarInitial(),
                profile.getSpecialty(),
                profile.getRating(),
                profile.getHourlyRate(),
                profile.getCompletedMissions(),
                profile.isAvailable()
        );

        return ResponseEntity.ok(response);
    }

    // ─── GET /internal/providers ──────────────────────────────────

    @GetMapping("/providers")
    @Operation(summary = "Liste des prestataires disponibles (filtre géo Haversine)")
    public ResponseEntity<List<ProviderSummary>> getProviders(
            @RequestParam(defaultValue = "0")  double lat,
            @RequestParam(defaultValue = "0")  double lng,
            @RequestParam(defaultValue = "10") double radiusKm,
            @RequestParam(required = false)    String specialty,
            @RequestParam(defaultValue = "0")  double minRating,
            @RequestParam(defaultValue = "0")  double maxRate) {

        List<ProviderProfile> profiles = providerProfileRepository
                .findAvailableInZone(lat, lng, radiusKm, specialty, minRating, maxRate);

        List<ProviderSummary> summaries = profiles.stream()
                .map(profile -> userRepository.findById(profile.getUserId())
                        .map(user -> new ProviderSummary(
                                UserIdFormatter.formatUserId(user.getId()),
                                user.getFirstName(),
                                user.getLastName(),
                                user.getFullName(),
                                user.getPhone(),
                                profile.getSpecialty(),
                                profile.getRating(),
                                profile.getHourlyRate(),
                                new com.serviloc.utilisateurs.application.dto.ProfileDtos.ServiceZone(
                                        profile.getServiceZoneCity(), profile.getRadiusKm()),
                                profile.isAvailable()
                        ))
                        .orElse(null))
                .filter(s -> s != null)
                .toList();

        return ResponseEntity.ok(summaries);
    }

    // ─── GET /internal/users/:id ──────────────────────────────────

    @GetMapping("/users/{id}")
    @Operation(summary = "Profil simplifié d'un utilisateur (inter-services)")
    public ResponseEntity<com.serviloc.utilisateurs.application.dto.ProfileDtos.InternalUserResponse> getUserById(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable : " + id));

        ProviderProfile providerProfile = user.getRole() == UserRole.PROVIDER
                ? providerProfileRepository.findByUserId(id).orElse(null)
                : null;

        return ResponseEntity.ok(UserResponseMapper.toInternalUserResponse(user, providerProfile));
    }

    // ─── POST /internal/users/:id/suspend ─────────────────────────

    @PostMapping("/users/{id}/suspend")
    @Operation(summary = "Suspension contextuelle depuis Service Litiges (UC31-agent)")
    public ResponseEntity<SuspendInternalResponse> suspendUser(
            @PathVariable UUID id,
            @Valid @RequestBody SuspendInternalRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable : " + id));

        user.suspend();
        userRepository.save(user);

        // Fix : ce endpoint injectait UserEventPublisher sans jamais l'appeler —
        // un utilisateur suspendu par un agent (UC31-agent) ne recevait donc
        // jamais de notification. Alignement sur le flux admin (AdminUserService).
        eventPublisher.publishUserSuspended(id, user.getEmail());

        log.info("[INTERNAL] Suspension : userId={} litigeId={} by={}",
                id, request.litigeId(), request.suspendedByRole());

        return ResponseEntity.ok(new SuspendInternalResponse(
                UserIdFormatter.formatUserId(id),
                "suspended",
                request.duration(),
                request.litigeId()
        ));
    }

    @GetMapping("/stats/users")
    @Operation(summary = "Statistiques utilisateurs pour /admin/stats")
    public ResponseEntity<UserStatsResponse> getUserStats() {
        long totalClients   = userJpaRepository.countByRole(UserRole.CLIENT);
        long totalProviders = userJpaRepository.countByRole(UserRole.PROVIDER);
        long totalAgents    = userJpaRepository.countByRole(UserRole.AGENT);

        return ResponseEntity.ok(new UserStatsResponse(totalClients, totalProviders, totalAgents));
    }

    public record UserStatsResponse(
            long totalClients,
            long totalProviders,
            long totalAgents
    ) {}

    // ─── PUT /internal/users/:id/rating ───────────────────────────

    @PutMapping("/users/{id}/rating")
    @Operation(summary = "Mise à jour du rating depuis Service Missions")
    public ResponseEntity<Void> updateRating(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRatingRequest request) {

        providerProfileRepository.findByUserId(id).ifPresent(profile -> {
            profile.updateRating(request.newRating());
            providerProfileRepository.save(profile);
            log.info("[INTERNAL] Rating mis à jour : userId={} rating={}", id, request.newRating());
        });

        return ResponseEntity.noContent().build();
    }

    // ─── DTOs internes ────────────────────────────────────────────

    public record SuspendInternalRequest(
            @NotBlank String reason,
            @NotNull String litigeId,
            @NotBlank String duration,        // "24h" | "7d" | "indefinite"
            @NotBlank String suspendedByRole  // "agent" | "admin"
    ) {}

    public record SuspendInternalResponse(
            String userId,
            String status,
            String duration,
            String litigeId
    ) {}

    public record UpdateRatingRequest(
            double newRating    // 0.0 - 5.0
    ) {}
}