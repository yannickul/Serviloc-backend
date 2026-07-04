package com.serviloc.notifications.presentation.dto;

import com.serviloc.notifications.application.dto.DeviceTokenResult;
import com.serviloc.notifications.domain.model.Platform;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Device token FCM enregistré")
public record RegisterDeviceTokenResponse(
        UUID id,
        String userId,
        Platform platform,
        String token,
        Instant updatedAt
) {
    public static RegisterDeviceTokenResponse from(DeviceTokenResult result) {
        return new RegisterDeviceTokenResponse(
                result.id(), result.userId(), result.platform(), result.token(), result.updatedAt());
    }
}
