package com.serviloc.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS global du Gateway. Un {@link CorsWebFilter} (plutôt que
 * spring.cloud.gateway.globalcors en YAML) est nécessaire car il s'applique à TOUTES
 * les requêtes WebFlux, y compris celles servies par un @RestController local comme
 * DashboardAggregatorController — spring.cloud.gateway.globalcors ne couvre que les
 * requêtes qui passent par le RouteLocator (les routes proxy déclarées en YAML).
 *
 * Ordre : HIGHEST_PRECEDENCE pour s'exécuter avant DashboardAuthWebFilter, sinon les
 * requêtes preflight OPTIONS (sans header Authorization) se feraient bloquer en 401
 * avant même d'atteindre la logique CORS.
 */
@Configuration
public class CorsConfig {

    // Liste séparée par virgules, ex: "http://localhost:5173,http://localhost:3000"
    @Value("${cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Correlation-Id"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
