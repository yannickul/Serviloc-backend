package com.serviloc.utilisateurs.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviloc.utilisateurs.application.dto.ProviderProfileDtos.*;
import com.serviloc.utilisateurs.domain.exception.UserNotFoundException;
import com.serviloc.utilisateurs.domain.model.ProviderProfile;
import com.serviloc.utilisateurs.domain.model.User;
import com.serviloc.utilisateurs.domain.repository.ProviderProfileRepository;
import com.serviloc.utilisateurs.domain.repository.UserRepository;
import com.serviloc.utilisateurs.infrastructure.messaging.UserEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ProviderProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProviderProfileService.class);

    private final UserRepository userRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final UserEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public ProviderProfileService(UserRepository userRepository,
                                  ProviderProfileRepository providerProfileRepository,
                                  UserEventPublisher eventPublisher,
                                  ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.providerProfileRepository = providerProfileRepository;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    // ─── PATCH /provider/profile ──────────────────────────────────

    public ProfileUpdatedResponse updateProfile(UUID userId,
                                                UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        ProviderProfile profile = providerProfileRepository.findByUserId(userId)
                .orElse(ProviderProfile.create(userId));

        profile.updateProfile(
                request.specialty(),
                request.hourlyRate(),
                request.serviceZoneCity(),
                request.latitude(),
                request.longitude(),
                request.radiusKm(),
                request.estCertifie(),
                request.certifications(),
                request.documentIds()
        );

        providerProfileRepository.save(profile);

        eventPublisher.publishProviderProfileUpdated(userId, user.getEmail());

        log.info("[PROVIDER] Profil mis à jour : userId={}", userId);

        return new ProfileUpdatedResponse(
                "usr_" + userId.toString().replace("-", "").substring(0, 8),
                "Profil mis à jour avec succès"
        );
    }

    // ─── PATCH /provider/availability ─────────────────────────────

    public AvailabilityUpdatedResponse updateAvailability(UUID userId,
                                                          UpdateAvailabilityRequest request) {
        ProviderProfile profile = providerProfileRepository.findByUserId(userId)
                .orElse(ProviderProfile.create(userId));

        profile.updateAvailability(request.isAvailable());
        providerProfileRepository.save(profile);

        log.info("[PROVIDER] Disponibilité mise à jour : userId={} available={}",
                userId, request.isAvailable());

        return new AvailabilityUpdatedResponse(
                "usr_" + userId.toString().replace("-", "").substring(0, 8),
                request.isAvailable(),
                "Disponibilité mise à jour"
        );
    }

    // ─── PATCH /provider/schedule ─────────────────────────────────

    public ScheduleUpdatedResponse updateSchedule(UUID userId,
                                                  UpdateScheduleRequest request) {
        ProviderProfile profile = providerProfileRepository.findByUserId(userId)
                .orElse(ProviderProfile.create(userId));

        try {
            String scheduleJson = objectMapper.writeValueAsString(request);
            profile.updateSchedule(scheduleJson);
            providerProfileRepository.save(profile);
        } catch (Exception e) {
            throw new IllegalArgumentException("Format d'horaires invalide");
        }

        log.info("[PROVIDER] Horaires mis à jour : userId={}", userId);

        return new ScheduleUpdatedResponse(
                "usr_" + userId.toString().replace("-", "").substring(0, 8),
                "Horaires mis à jour avec succès"
        );
    }
}