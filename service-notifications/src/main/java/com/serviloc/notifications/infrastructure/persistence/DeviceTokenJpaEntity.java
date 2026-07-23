package com.serviloc.notifications.infrastructure.persistence;

import com.serviloc.notifications.domain.model.Platform;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "device_tokens", uniqueConstraints = {
        @UniqueConstraint(name = "uk_device_tokens_platform_token", columnNames = {"platform", "token"})
})
public class DeviceTokenJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Platform platform;

    @Column(nullable = false, length = 512)
    private String token;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected DeviceTokenJpaEntity() {
        // requis par JPA
    }

    public DeviceTokenJpaEntity(UUID id, String userId, Platform platform, String token, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.platform = platform;
        this.token = token;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Platform getPlatform() {
        return platform;
    }

    public String getToken() {
        return token;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
