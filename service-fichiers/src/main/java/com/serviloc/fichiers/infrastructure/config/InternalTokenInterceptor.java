package com.serviloc.fichiers.infrastructure.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Vérifie la présence et la validité du header {@code X-Internal-Token} sur
 * toutes les requêtes {@code /internal/**}. Ces endpoints ne sont jamais
 * exposés par le Gateway ; ils ne circulent que sur le réseau Docker interne
 * (voir section 4.4 de l'architecture — token machine partagé injecté
 * automatiquement côté appelant par un RequestInterceptor Feign).
 */
@Component
public class InternalTokenInterceptor implements HandlerInterceptor {

    @Value("${serviloc.internal-token}")
    private String internalToken;

    private static final String HEADER = "X-Internal-Token";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws java.io.IOException {
        String token = request.getHeader(HEADER);
        if (token == null || !token.equals(internalToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\","
                            + "\"message\":\"Token interne manquant ou invalide\",\"field\":null}}");
            return false;
        }
        return true;
    }
}
