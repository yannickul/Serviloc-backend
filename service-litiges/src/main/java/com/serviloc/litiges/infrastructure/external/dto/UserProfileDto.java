// infrastructure/external/dto/UserProfileDto.java
package com.serviloc.litiges.infrastructure.external.dto;

/**
 * ⚠️ Format provisoire — le contrat interne exact de service-utilisateurs pour
 * /internal/users/{id} ne figure pas dans l'API_CONTRACT ni dans les notes fournies.
 * À confirmer avec l'équipe service-utilisateurs et ajuster les champs si besoin.
 */
public record UserProfileDto(
        String id,
        String name,
        String email,
        String role
) {
    public static UserProfileDto unknown(String id) {
        return new UserProfileDto(id, null, null, null);
    }
}
