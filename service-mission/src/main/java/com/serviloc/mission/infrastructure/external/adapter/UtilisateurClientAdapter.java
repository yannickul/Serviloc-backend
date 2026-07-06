package com.serviloc.mission.infrastructure.external.adapter;

import com.serviloc.mission.application.port.out.ProviderCachePort;
import com.serviloc.mission.application.port.out.ProviderPort;
import com.serviloc.mission.infrastructure.external.ProviderDto;
import com.serviloc.mission.infrastructure.external.ProviderSummary;
import com.serviloc.mission.infrastructure.external.UtilisateurClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class UtilisateurClientAdapter implements ProviderPort {

    private static final Logger log =
            LoggerFactory.getLogger(UtilisateurClientAdapter.class);

    private final UtilisateurClient utilisateurClient;
    private final ProviderCachePort cachePort;
    private final CircuitBreaker circuitBreaker;

    public UtilisateurClientAdapter(
            UtilisateurClient utilisateurClient,
            ProviderCachePort cachePort,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.utilisateurClient = utilisateurClient;
        this.cachePort = cachePort;
        this.circuitBreaker = circuitBreakerRegistry
                .circuitBreaker("utilisateurClient");
    }

    @Override
    public List<ProviderSummary> searchProviders(
            double lat, double lng, String categoryId) {

        String cacheKey = buildCacheKey(lat, lng, categoryId);

        Supplier<List<ProviderSummary>> supplier = CircuitBreaker
                .decorateSupplier(circuitBreaker, () -> {
                    List<ProviderSummary> providers = utilisateurClient
                            .getProviders(lat, lng, 20, categoryId);
                    cachePort.cacheProviders(cacheKey, providers);
                    return providers;
                });

        try {
            return supplier.get();
        } catch (Exception e) {
            log.warn("Circuit breaker activé — service-utilisateurs : {}",
                    e.getMessage());
            List<ProviderSummary> cached = cachePort.getCachedProviders(cacheKey);
            if (cached != null) {
                log.info("Retour cache Redis — {} prestataires", cached.size());
                return cached;
            }
            log.warn("Cache vide — liste vide retournée");
            return Collections.emptyList();
        }
    }

    private String buildCacheKey(double lat, double lng, String categoryId) {
        return String.format("providers:lat:%.4f:lng:%.4f:cat:%s",
                lat, lng, categoryId);
    }

    private ProviderSummary toSummary(ProviderDto dto) {
        ProviderSummary summary = new ProviderSummary();
        summary.setId(dto.id());
        summary.setFullName(dto.fullName());
        summary.setSpecialty(dto.specialty());
        summary.setRating(dto.rating());
        summary.setHourlyRate(dto.hourlyRate());
        summary.setDistanceKm(dto.distanceKm());
        summary.setAvailable(dto.isAvailable());
        summary.setCompletedMissions(dto.completedMissions());
        return summary;
    }
}