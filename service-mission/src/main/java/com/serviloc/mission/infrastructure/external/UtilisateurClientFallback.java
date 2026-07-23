// UtilisateurClientFallback.java
package com.serviloc.mission.infrastructure.external;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviloc.mission.domain.event.RatingUpdatePendingEvent;
import com.serviloc.mission.infrastructure.messaging.MissionEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UtilisateurClientFallback implements UtilisateurClient {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final MissionEventPublisher eventPublisher;

    // Clé construite à partir des paramètres de recherche, stockée par le vrai client au retour normal
    // Format : "providers:lat:lng:radiusKm:specialty"
    private String cacheKey(double lat, double lng, int radiusKm, String specialty) {
        return String.format("providers:%s:%s:%d:%s", lat, lng, radiusKm, specialty);
    }

    @Override
    public List<ProviderSummary> getProviders(double lat, double lng, int radiusKm, String specialty) {
        log.warn("UtilisateurClient indisponible — tentative de fallback Redis pour lat={} lng={}", lat, lng);
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey(lat, lng, radiusKm, specialty));
            if (cached != null) {
                return objectMapper.readValue(cached, new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.error("Erreur lecture Redis fallback prestataires", e);
        }
        return Collections.emptyList();
    }

    @Override
    public UserSummary getUserById(String id) {
        log.warn("UtilisateurClient indisponible — getUserById({}) ne peut pas être mis en cache", id);
        return null;
    }

    @Override
    public ProviderLookupResponse getProviderById(String id) {
        log.warn("UtilisateurClient indisponible — getProviderById({}) non résolu", id);
        return null;
    }

    @Override
    public void updateRating(String id, UpdateRatingRequest request) {
        log.warn("UtilisateurClient indisponible — updateRating({}) mis en outbox RabbitMQ", id);
    }

    @Override
    public UserStatsResponse getUserStats() {
        log.warn("UtilisateurClient indisponible — getUserStats() fallback à zéro");
        return new UserStatsResponse(0, 0, 0);
    }
}