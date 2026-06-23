package com.serviloc.notifications.infrastructure.persistence;

import com.serviloc.notifications.domain.model.DeviceToken;
import com.serviloc.notifications.domain.model.Platform;
import com.serviloc.notifications.domain.repository.DeviceTokenRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DeviceTokenRepositoryAdapter implements DeviceTokenRepository {

    private final DeviceTokenJpaRepository jpaRepository;

    public DeviceTokenRepositoryAdapter(DeviceTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public DeviceToken save(DeviceToken deviceToken) {
        DeviceTokenJpaEntity entity = new DeviceTokenJpaEntity(
                deviceToken.getId(),
                deviceToken.getUserId(),
                deviceToken.getPlatform(),
                deviceToken.getToken(),
                deviceToken.getUpdatedAt());
        DeviceTokenJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<DeviceToken> findByPlatformAndToken(Platform platform, String token) {
        return jpaRepository.findByPlatformAndToken(platform, token).map(this::toDomain);
    }

    @Override
    public List<DeviceToken> findAllByUserId(String userId) {
        return jpaRepository.findAllByUserId(userId).stream().map(this::toDomain).toList();
    }

    private DeviceToken toDomain(DeviceTokenJpaEntity entity) {
        return DeviceToken.reconstitute(entity.getId(), entity.getUserId(), entity.getPlatform(),
                entity.getToken(), entity.getUpdatedAt());
    }
}
