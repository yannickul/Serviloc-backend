package com.serviloc.negociations.infrastructure.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Client interne vers service-utilisateurs — résolution fullName/avatarInitial/rating/specialty
 * pour l'affichage enrichi des conversations (audit front).
 *
 * rating/specialty sont désormais renvoyés par GET /internal/users/{id} (confirmé par
 * service-utilisateurs, branche developer, Juillet 2026) — null pour un client, valeurs
 * réelles pour un prestataire.
 */
@FeignClient(name = "service-utilisateurs", fallback = UtilisateursClientFallback.class)
public interface UtilisateursClient {

    @GetMapping("/internal/users/{id}")
    UserSummaryResponse getUserById(
            @PathVariable("id") String id,
            @RequestHeader("X-Internal-Token") String internalToken
    );

    /** Réponse de service-utilisateurs (branche developer) — GET /internal/users/{id}. */
    record UserSummaryResponse(
            String id,
            String role,
            String firstName,
            String lastName,
            String fullName,
            String phone,
            String email,
            String avatarInitial,
            String status,
            Double rating,
            String specialty,
            String createdAt
    ) {}
}
