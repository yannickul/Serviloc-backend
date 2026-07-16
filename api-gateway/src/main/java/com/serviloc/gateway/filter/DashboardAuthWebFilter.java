package com.serviloc.gateway.filter;

import com.serviloc.gateway.config.GatewayJwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * Sécurité JWT + rôle pour les endpoints d'agrégation (DashboardAggregatorController).
 *
 * Ces endpoints sont des @RestController "locaux" au Gateway (ils ne passent PAS par le
 * RouteLocator / une Route déclarée en YAML), donc n'héritent PAS des GatewayFilters
 * JwtAuthFilter / RoleAuthFilter appliqués route par route. Ce WebFilter reproduit la même
 * vérification (mêmes réponses 401/403, mêmes headers X-User-Id / X-User-Role injectés en
 * aval) pour garder un comportement de sécurité identique au reste du Gateway.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class DashboardAuthWebFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(DashboardAuthWebFilter.class);

    // Chemin exact → rôle requis (en majuscules, cf. JWT v2.1)
    private static final Map<String, String> PATH_ROLE_MAP = Map.of(
            "/v1/client/dashboard",   "CLIENT",
            "/v1/provider/dashboard", "PROVIDER",
            "/v1/admin/dashboard",    "ADMIN",
            "/v1/admin/stats",        "ADMIN"
    );

    private final GatewayJwtService jwtService;

    public DashboardAuthWebFilter(GatewayJwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        String requiredRole = PATH_ROLE_MAP.get(path);
        if (requiredRole == null) {
            return chain.filter(exchange);
        }

        String correlationId = request.getHeaders().getFirst("X-Correlation-Id");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        final String finalCorrelationId = correlationId;

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[Gateway-Dashboard] Token absent → {}", path);
            return unauthorized(exchange, "Token d'authentification manquant");
        }

        try {
            Claims claims = jwtService.validateAndExtract(authHeader.substring(7));
            String userId = claims.get("userId", String.class);
            if (userId == null || userId.isBlank()) {
                userId = claims.getSubject();
            }
            String role = claims.get("role", String.class);

            if (role == null || !requiredRole.equalsIgnoreCase(role)) {
                log.warn("[Gateway-Dashboard] Accès refusé : path={} requiredRole={} userRole={}",
                        path, requiredRole, role);
                return forbidden(exchange, "Accès refusé. Rôle requis : " + requiredRole.toLowerCase());
            }

            ServerHttpRequest mutated = request.mutate()
                    .header("X-User-Id",       userId != null ? userId : "")
                    .header("X-User-Role",     role)
                    .header("X-Correlation-Id", finalCorrelationId)
                    .build();

            return chain.filter(exchange.mutate().request(mutated).build());

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[Gateway-Dashboard] JWT invalide → {} : {}", path, e.getMessage());
            return unauthorized(exchange, "Token invalide ou expiré");
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        return writeError(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized", message);
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        return writeError(exchange, HttpStatus.FORBIDDEN, "Forbidden", message);
    }

    private Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, String error, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"status":%d,"error":"%s","message":"%s"}
                """.formatted(status.value(), error, message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
