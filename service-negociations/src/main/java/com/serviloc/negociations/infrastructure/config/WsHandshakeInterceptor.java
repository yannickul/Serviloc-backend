package com.serviloc.negociations.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Le JWT est déjà validé par le Gateway (JwtAuthFilter) avant que la requête
 * n'atteigne ce service — comme pour tous les endpoints REST. On récupère ici
 * simplement les headers X-User-Id / X-User-Role injectés par le Gateway et on
 * les place dans les attributs de session WebSocket pour un usage ultérieur
 * (ex. WsChannelInterceptor, autorisation des abonnements).
 */
@Component
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WsHandshakeInterceptor.class);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        String userId = request.getHeaders().getFirst("X-User-Id");
        String role   = request.getHeaders().getFirst("X-User-Role");

        if (userId == null || userId.isBlank()) {
            log.warn("[WS] Handshake refusé — X-User-Id manquant (Gateway a-t-il bien traité la requête ?)");
            return false;
        }

        attributes.put("userId", userId);
        attributes.put("userRole", role);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
