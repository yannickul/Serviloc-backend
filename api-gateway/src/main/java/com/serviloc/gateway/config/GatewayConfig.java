package com.serviloc.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    private static final Logger log = LoggerFactory.getLogger(GatewayConfig.class);

    @Value("${rate-limiting.replenish-rate:10}")
    private int replenishRate;

    @Value("${rate-limiting.burst-capacity:20}")
    private int burstCapacity;

    // ─── Key Resolver ─────────────────────────────────────────────
    // Priorité : X-User-Id (authentifié) > IP (public)

    @Bean
    public KeyResolver rateLimitKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null && !userId.isBlank()) {
                return Mono.just("user:" + userId);
            }
            String ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            return Mono.just("ip:" + ip);
        };
    }

    // ─── Rate Limiter Redis ───────────────────────────────────────
    // Token Bucket : replenishRate tokens/s, burst max burstCapacity

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(replenishRate, burstCapacity, 1);
    }

    // ─── Fallback circuit breaker ─────────────────────────────────
    // Appelé quand un service downstream est indisponible

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