package com.serviloc.notifications.application.dto;

import com.serviloc.notifications.domain.model.Platform;

/**
 * Commande d'enregistrement d'un token FCM, indépendante du transport REST.
 */
public record RegisterDeviceTokenCommand(String userId, Platform platform, String token) {
}
