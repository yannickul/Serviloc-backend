package com.serviloc.notifications.presentation.controller;

import com.serviloc.notifications.application.dto.DeviceTokenResult;
import com.serviloc.notifications.application.dto.RegisterDeviceTokenCommand;
import com.serviloc.notifications.application.port.in.RegisterDeviceTokenUseCase;
import com.serviloc.notifications.presentation.dto.ApiResponse;
import com.serviloc.notifications.presentation.dto.RegisterDeviceTokenRequest;
import com.serviloc.notifications.presentation.dto.RegisterDeviceTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints internes du Service Notifications — appelés uniquement par les autres
 * microservices ServiLoc (jamais exposés au frontend via le Gateway public).
 */
@RestController
@RequestMapping("/internal/device-tokens")
@Tag(name = "Internal - Device Tokens", description = "Enregistrement des tokens FCM par les autres microservices")
public class DeviceTokenController {

    private final RegisterDeviceTokenUseCase registerDeviceTokenUseCase;

    public DeviceTokenController(RegisterDeviceTokenUseCase registerDeviceTokenUseCase) {
        this.registerDeviceTokenUseCase = registerDeviceTokenUseCase;
    }

    @PostMapping
    @Operation(summary = "Enregistre (upsert) un token FCM pour un utilisateur",
            description = "Appelé par le Service Utilisateurs lors de la connexion mobile. "
                    + "Si le couple (platform, token) existe déjà, met simplement à jour userId/updatedAt.")
    public ResponseEntity<ApiResponse<RegisterDeviceTokenResponse>> registerDeviceToken(
            @Valid @RequestBody RegisterDeviceTokenRequest request) {

        RegisterDeviceTokenCommand command = new RegisterDeviceTokenCommand(
                request.userId(), request.platform(), request.token());

        DeviceTokenResult result = registerDeviceTokenUseCase.registerDeviceToken(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(RegisterDeviceTokenResponse.from(result)));
    }
}
