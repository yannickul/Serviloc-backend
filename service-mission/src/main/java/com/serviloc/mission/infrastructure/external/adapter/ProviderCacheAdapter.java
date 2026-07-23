// infrastructure/external/adapter/ProviderCacheAdapter.java
package com.serviloc.mission.infrastructure.external.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviloc.mission.application.port.out.ProviderCachePort;
import com.serviloc.mission.infrastructure.external.ProviderSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class ProviderCacheAdapter implements ProviderCachePort {

    private static final Logger log =
            LoggerFactory.getLogger(ProviderCacheAdapter.class);
    private static final long TTL_MINUTES = 5;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public ProviderCacheAdapter(
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ProviderSummary> getCachedProviders(String cacheKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached == null) return null;
            return objectMapper.convertValue(
                    cached,
                    objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, ProviderSummary.class)
            );
        } catch (Exception e) {
            log.warn("Erreur lecture Redis clé {} : {}", cacheKey, e.getMessage());
            return null;
        }
    }

    @Override
    public void cacheProviders(String cacheKey, List<ProviderSummary> providers) {
        try {
            redisTemplate.opsForValue()
                    .set(cacheKey, providers, TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Erreur écriture Redis clé {} : {}", cacheKey, e.getMessage());
        }
    }
}