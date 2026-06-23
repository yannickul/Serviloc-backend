package com.serviloc.notifications.application.port.in;

import com.serviloc.notifications.application.dto.DeviceTokenResult;
import com.serviloc.notifications.application.dto.RegisterDeviceTokenCommand;

/**
 * Use case : enregistrement (upsert) d'un token FCM pour un utilisateur.
 * Correspond à {@code POST /internal/device-tokens}.
 */
public interface RegisterDeviceTokenUseCase {

    DeviceTokenResult registerDeviceToken(RegisterDeviceTokenCommand command);
}
