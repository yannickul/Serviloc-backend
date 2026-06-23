package com.serviloc.notifications.presentation.dto;

import com.serviloc.notifications.domain.model.Platform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Requête {@code POST /internal/device-tokens} — cf. architecture §3.6.
 */
public record RegisterDeviceTokenRequest(

        @Schema(description = "Identifiant utilisateur ServiLoc", example = "usr_abc123")
        @NotBlank(message = "userId est obligatoire")
        String userId,

        @Schema(description = "Plateforme de l'appareil")
        @NotNull(message = "platform est obligatoire")
        Platform platform,

        @Schema(description = "Token d'enregistrement FCM de l'appareil")
        @NotBlank(message = "token est obligatoire")
        String token
) {
}
