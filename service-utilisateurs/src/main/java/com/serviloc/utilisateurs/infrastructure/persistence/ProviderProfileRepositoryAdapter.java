package com.serviloc.utilisateurs.infrastructure.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviloc.utilisateurs.domain.model.ProviderProfile;
import com.serviloc.utilisateurs.domain.repository.ProviderProfileRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ProviderProfileRepositoryAdapter implements ProviderProfileRepository {

    private final ProviderProfileJpaRepository jpa;
    private final ObjectMapper objectMapper;

    public ProviderProfileRepositoryAdapter(ProviderProfileJpaRepository jpa,
                                            ObjectMapper objectMapper) {
        this.jpa = jpa;
        this.objectMapper = objectMapper;
    }

    @Override
    public ProviderProfile save(ProviderProfile profile) {
        ProviderProfileJpaEntity entity = jpa.findByUserId(profile.getUserId())
                .orElse(new ProviderProfileJpaEntity(profile.getId(), profile.getUserId()));

        entity.setSpecialty(profile.getSpecialty());
        entity.setHourlyRate(profile.getHourlyRate());
        entity.setServiceZoneCity(profile.getServiceZoneCity());
        entity.setRadiusKm(profile.getRadiusKm());
        entity.setEstCertifie(profile.isEstCertifie());
        entity.setAvailable(profile.isAvailable());
        entity.setRating(profile.getRating());
        entity.setCompletedMissions(profile.getCompletedMissions());
        entity.setMonthlyEarnings(profile.getMonthlyEarnings());
        entity.setCertificationsJson(toJson(profile.getCertifications()));
        entity.setDocumentIdsJson(toJson(profile.getDocumentIds()));
        entity.setWeeklyScheduleJson(profile.getWeeklyScheduleJson());

        return toDomain(jpa.save(entity));
    }

    @Override
    public Optional<ProviderProfile> findByUserId(UUID userId) {
        return jpa.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public List<ProviderProfile> findAvailableInZone(double lat, double lng,
                                                     double radiusKm, String specialty,
                                                     double minRating, double maxRate) {
        return jpa.findAvailableInZone(lat, lng, radiusKm, specialty, minRating, maxRate)
                .stream().map(this::toDomain).toList();
    }

    // ─── Mappers ──────────────────────────────────────────────────

    private ProviderProfile toDomain(ProviderProfileJpaEntity e) {
        try {
            var ctor = ProviderProfile.class.getDeclaredConstructor(
                    UUID.class, UUID.class, String.class, double.class, int.class,
                    boolean.class, double.class, String.class, double.class,
                    boolean.class, List.class, List.class, double.class, String.class
            );
            ctor.setAccessible(true);
            return ctor.newInstance(
                    e.getId(), e.getUserId(), e.getSpecialty(),
                    e.getRating(), e.getCompletedMissions(),
                    e.isAvailable(), e.getHourlyRate(),
                    e.getServiceZoneCity(), e.getRadiusKm(),
                    e.isEstCertifie(),
                    fromJson(e.getCertificationsJson()),
                    fromJson(e.getDocumentIdsJson()),
                    e.getMonthlyEarnings(),
                    e.getWeeklyScheduleJson()
            );
        } catch (Exception ex) {
            throw new RuntimeException("Erreur reconstitution ProviderProfile", ex);
        }
    }

    private String toJson(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}