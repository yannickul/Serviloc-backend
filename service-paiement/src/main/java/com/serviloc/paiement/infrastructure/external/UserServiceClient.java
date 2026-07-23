package com.serviloc.paiement.infrastructure.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Client Feign vers service-utilisateurs, utilisé pour résoudre
 * providerName / clientName dans GET /admin/stats.
 * Découverte via Eureka (nom du service enregistré : "service-utilisateurs").
 */
@FeignClient(name = "service-utilisateurs")
public interface UserServiceClient {

    @GetMapping("/internal/users/{id}")
    UserSummary getUserById(@PathVariable("id") String id,
                            @RequestHeader("X-Internal-Token") String internalToken);

    record UserSummary(
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
