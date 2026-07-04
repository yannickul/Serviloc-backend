package com.serviloc.notifications.domain.repository;

import com.serviloc.notifications.domain.model.DeviceToken;
import com.serviloc.notifications.domain.model.Platform;

import java.util.List;
import java.util.Optional;

/**
 * Port de persistance pour {@link DeviceToken} (implémenté en infrastructure/persistence).
 */
public interface DeviceTokenRepository {

    DeviceToken save(DeviceToken deviceToken);

    Optional<DeviceToken> findByPlatformAndToken(Platform platform, String token);

    List<DeviceToken> findAllByUserId(String userId);
}
