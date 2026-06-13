package com.serviloc.gateway.filter;

import com.serviloc.gateway.config.GatewayJwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final GatewayJwtService jwtService;

    public JwtAuthFilter(GatewayJwtService jwtService) {
        super(Config.class);
        this.jwtService = jwtService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // ─── Génération / propagation X-Correlation-Id ────────
            String correlationId = request.getHeaders().getFirst("X-Correlation-Id");
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }
            final String finalCorrelationId = correlationId;

            // ─── Extraction du token ──────────────────────────────
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("[Gateway] Token absent → {}", request.getURI().getPath());
                return unauthorized(exchange, "Token d'authentification manquant");
            }

            String token = authHeader.substring(7);

            // ─── Validation JWT ───────────────────────────────────
            try {
                Claims claims = jwtService.validateAndExtract(token);
                String userId = claims.get("userId", String.class);
                if (userId == null || userId.isBlank()) {
                    userId = claims.getSubject(); // fallback si ancien token
                }
                String role   = claims.get("role", String.class);

                log.debug("[Gateway] JWT valide → userId={} role={}", userId, role);

                // ─── Injection des headers downstream ─────────────
                ServerHttpRequest mutated = request.mutate()
                        .header("X-User-Id",        userId != null ? userId : "")
                        .header("X-User-Role",       role   != null ? role   : "")
                        .header("X-Correlation-Id",  finalCorrelationId)
                        .build();

                return chain.filter(exchange.mutate().request(mutated).build());

            } catch (JwtException | IllegalArgumentException e) {
                log.warn("[Gateway] JWT invalide → {} : {}", request.getURI().getPath(), e.getMessage());
                return unauthorized(exchange, "Token invalide ou expiré");
            }
        };
    }

    // ─── Réponse 401 JSON ─────────────────────────────────────────
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"status":401,"error":"Unauthorized","message":"%s"}
                """.formatted(message);
        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // Pas de configuration spécifique pour ce filtre
    }
}