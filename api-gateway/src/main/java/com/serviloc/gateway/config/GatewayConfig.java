package com.serviloc.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;

@Configuration
public class GatewayConfig {

    private static final Logger log = LoggerFactory.getLogger(GatewayConfig.class);

    @Value("${rate-limiting.replenish-rate:10}")
    private int replenishRate;

    @Value("${rate-limiting.burst-capacity:20}")
    private int burstCapacity;

    // ─── Key Resolver ─────────────────────────────────────────────
    // Priorité : X-User-Id (authentifié) > IP (public)


    // ─── Rate Limiter — config par défaut (surchargée par route dans application.yml)
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20, 1); // valeurs par défaut, ignorées si args fournis en YAML
    }

    @Bean
    public KeyResolver roleKeyResolver() {
        return exchange -> {
            String role = exchange.getRequest().getHeaders().getFirst("X-User-Role");
            if (role != null && !role.isBlank()) {
                return Mono.just("role:" + role.toUpperCase());
            }
            String ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            return Mono.just("ip:" + ip);
        };
    }


    @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        return RouterFunctions.route()
                .GET("/fallback",    req -> fallbackResponse())
                .POST("/fallback",   req -> fallbackResponse())
                .PUT("/fallback",    req -> fallbackResponse())
                .DELETE("/fallback", req -> fallbackResponse())
                .build();
    }

    private Mono<ServerResponse> fallbackResponse() {
        log.warn("[Gateway] Fallback déclenché — service indisponible");
        String body = """
                {
                  "status": 503,
                  "error": "Service Unavailable",
                  "message": "Service temporairement indisponible. Réessayez dans quelques secondes."
                }
                """;
        return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body);
    }
}