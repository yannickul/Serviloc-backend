package com.serviloc.notifications.application.dto;

import com.serviloc.notifications.domain.model.Platform;

import java.time.Instant;
import java.util.UUID;

public record DeviceTokenResult(UUID id, String userId, Platform platform, String token, Instant updatedAt) {
}
