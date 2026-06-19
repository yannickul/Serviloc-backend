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
import java.util.List;
import java.util.Map;

/**
 * Filtre de vérification du rôle JWT par préfixe de route.
 * S'applique APRÈS JwtAuthFilter qui a déjà injecté X-User-Role.
 *
 * Mapping :
 *   /client/**   → role == CLIENT
 *   /provider/** → role == PROVIDER
 *   /agent/**    → role == AGENT
 *   /admin/**    → role == ADMIN
 */
@Component
public class RoleAuthFilter extends AbstractGatewayFilterFactory<RoleAuthFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(RoleAuthFilter.class);

    // Mapping préfixe → rôle attendu (en majuscules)
    private static final Map<String, String> PREFIX_ROLE_MAP = Map.of(
            "/client",   "CLIENT",
            "/provider", "PROVIDER",
            "/agent",    "AGENT",
            "/admin",    "ADMIN"
    );

    private final GatewayJwtService jwtService;

    public RoleAuthFilter(GatewayJwtService jwtService) {
        super(Config.class);
        this.jwtService = jwtService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // Détermine le rôle requis selon le préfixe
            String requiredRole = getRequiredRole(path);
            if (requiredRole == null) {
                // Pas de restriction de rôle pour ce chemin
                return chain.filter(exchange);
            }

            // Récupère le rôle depuis le header X-User-Role
            // (déjà injecté par JwtAuthFilter)
            String userRole = request.getHeaders().getFirst("X-User-Role");

            if (userRole == null || userRole.isBlank()) {
                // Essaie d'extraire depuis le token directement
                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    try {
                        Claims claims = jwtService.validateAndExtract(authHeader.substring(7));
                        userRole = claims.get("role", String.class);
                    } catch (JwtException e) {
                        return forbidden(exchange, "Token invalide");
                    }
                }
            }

            if (userRole == null || !requiredRole.equalsIgnoreCase(userRole)) {
                log.warn("[Gateway-Role] Accès refusé : path={} requiredRole={} userRole={}",
                        path, requiredRole, userRole);
                return forbidden(exchange,
                        "Accès refusé. Rôle requis : " + requiredRole.toLowerCase());
            }

            log.debug("[Gateway-Role] Accès autorisé : path={} role={}", path, userRole);
            return chain.filter(exchange);
        };
    }

    // ─── Helpers ──────────────────────────────────────────────────

    private String getRequiredRole(String path) {
        return PREFIX_ROLE_MAP.entrySet().stream()
                .filter(entry -> path.contains(entry.getKey() + "/") ||
                        path.endsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = """
            {"status":403,"error":"Forbidden","message":"%s"}
            """.formatted(message);
        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {}
}