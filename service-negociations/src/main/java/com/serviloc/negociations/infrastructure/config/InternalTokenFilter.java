package com.serviloc.negociations.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(2)
public class InternalTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(InternalTokenFilter.class);
    private static final String HEADER = "X-Internal-Token";

    @Value("${internal.token}")
    private String internalToken;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Applique uniquement sur /internal/**
        if (!path.startsWith("/internal/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader(HEADER);

        if (token == null || !token.equals(internalToken)) {
            log.warn("[INTERNAL-FILTER] Accès refusé : path={} token={}", path,
                    token != null ? "présent mais invalide" : "absent");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\":false,\"error\":{\"code\":\"MISSING_HEADER\",\"message\":\"En-tete requis manquant : X-Internal-Token\"}}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}