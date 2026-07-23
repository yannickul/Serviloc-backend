package com.serviloc.paiement.infrastructure.external;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Résout un nom d'affichage (fullName) pour un providerId/clientId via
 * service-utilisateurs. En cas d'indisponibilité du service ou d'utilisateur
 * introuvable, retombe sur l'ID brut (décision produit : afficher l'ID en fallback).
 */
@Component
public class UserResolver {

    private static final Logger log = LoggerFactory.getLogger(UserResolver.class);

    private final UserServiceClient client;

    @Value("${internal.token}")
    private String internalToken;

    public UserResolver(UserServiceClient client) {
        this.client = client;
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackName")
    public String resolveFullName(UUID userId) {
        UserServiceClient.UserSummary user = client.getUserById(userId.toString(), internalToken);
        if (user == null || user.fullName() == null || user.fullName().isBlank()) {
            return userId.toString();
        }
        return user.fullName();
    }

    @SuppressWarnings("unused")
    private String fallbackName(UUID userId, Throwable ex) {
        log.warn("[USER-RESOLVER] service-utilisateurs indisponible, fallback sur l'ID : userId={} cause={}",
                userId, ex.toString());
        return userId.toString();
    }
}
