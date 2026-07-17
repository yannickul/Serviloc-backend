package com.serviloc.utilisateurs.application.dto;

import jakarta.validation.constraints.Pattern;

public final class ClientProfileDtos {

    public record UpdateClientProfileRequest(
            String firstName,
            String lastName,
            @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Numéro invalide")
            String phone,
            String avatarUrl
    ) {}

    public record ClientProfileUpdatedResponse(
            String clientId,
            String message
    ) {}
}