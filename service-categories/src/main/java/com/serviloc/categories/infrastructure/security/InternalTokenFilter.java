package com.serviloc.categories.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Protège les endpoints /internal/** : appels réservés aux autres microservices
 * (Feign), authentifiés par un token machine partagé, jamais exposés hors du
 * réseau Docker interne (voir ARCHITECTURE §4.4).
 */
@Component
public class InternalTokenFilter extends OncePerRequestFilter {

    public static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private final String expectedToken;

    public InternalTokenFilter(@Value("${serviloc.internal-token}") String expectedToken) {
        this.expectedToken = expectedToken;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String providedToken = request.getHeader(INTERNAL_TOKEN_HEADER);
        if (providedToken == null || !providedToken.equals(expectedToken)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("""
                    {"success":false,"error":{"code":"ACCESS_DENIED","message":"Accès interne refusé","field":null}}
                    """);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
