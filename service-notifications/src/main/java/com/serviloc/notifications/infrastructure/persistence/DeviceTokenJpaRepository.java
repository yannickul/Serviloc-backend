package com.serviloc.notifications.infrastructure.persistence;

import com.serviloc.notifications.domain.model.Platform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenJpaRepository extends JpaRepository<DeviceTokenJpaEntity, UUID> {

    Optional<DeviceTokenJpaEntity> findByPlatformAndToken(Platform platform, String token);

    List<DeviceTokenJpaEntity> findAllByUserId(String userId);
}
