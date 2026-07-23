package com.serviloc.fichiers.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Sérialisation JSON (Jackson) des valeurs mises en cache Redis, plus robuste
 * et lisible que la sérialisation Java par défaut — nécessaire car les DTO
 * cachés ({@link com.serviloc.fichiers.application.dto.FileMetadataResponse})
 * sont des records ne déclarant pas Serializable.
 */
@Configuration
public class RedisCacheConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .activateDefaultTyping(
                        com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator.builder()
                                .allowIfBaseType(Object.class).build(),
                        // EVERYTHING (et non NON_FINAL) : les DTO mis en cache sont des records
                        // Java, donc implicitement `final`. Avec NON_FINAL, Jackson n'écrit
                        // l'info de type ("@class") QUE pour les classes non-finales, ce qui
                        // fait qu'à la relecture depuis Redis, un record revient sous forme de
                        // LinkedHashMap brute (perte du type) → ClassCastException → 500 au
                        // deuxième appel (cache hit) sur un endpoint interne.
                        ObjectMapper.DefaultTyping.EVERYTHING);

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(mapper);

        return builder -> builder.cacheDefaults(
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(10))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                        .disableCachingNullValues());
    }
}
