package com.serviloc.notifications.domain.model;

import com.serviloc.notifications.domain.exception.InvalidDeviceTokenException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Token d'appareil (FCM) associé à un utilisateur, utilisé pour l'envoi de notifications push.
 *
 * Invariant : un même token physique (couple platform+token) est unique pour un utilisateur donné ;
 * un enregistrement répété met simplement à jour {@code updatedAt} (upsert), géré par le repository.
 */
public class DeviceToken {

    private final UUID id;
    private String userId;
    private final Platform platform;
    private final String token;
    private Instant updatedAt;

    private DeviceToken(UUID id, String userId, Platform platform, String token, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.platform = platform;
        this.token = token;
        this.updatedAt = updatedAt;
    }

    /**
     * Enregistre un nouveau token d'appareil pour un utilisateur.
     */
    public static DeviceToken register(String userId, Platform platform, String token) {
        validate(userId, platform, token);
        return new DeviceToken(UUID.randomUUID(), userId, platform, token, Instant.now());
    }

    /**
     * Reconstruction depuis la persistance.
     */
    public static DeviceToken reconstitute(UUID id, String userId, Platform platform, String token, Instant updatedAt) {
        validate(userId, platform, token);
        return new DeviceToken(id, userId, platform, token, updatedAt);
    }

    private static void validate(String userId, Platform platform, String token) {
        if (userId == null || userId.isBlank()) {
            throw new InvalidDeviceTokenException("userId est obligatoire pour enregistrer un device token");
        }
        if (platform == null) {
            throw new InvalidDeviceTokenException("platform est obligatoire (ANDROID, IOS ou WEB)");
        }
        if (token == null || token.isBlank()) {
            throw new InvalidDeviceTokenException("token FCM est obligatoire et ne peut être vide");
        }
    }

    /** Rafraîchit la date de mise à jour (cas d'un upsert sur token déjà connu). */
    public void touch() {
        this.updatedAt = Instant.now();
    }

    /**
     * Réassigne ce token physique à un autre utilisateur (ex: déconnexion/reconnexion d'un compte
     * différent sur le même appareil) tout en conservant l'identité de l'enregistrement.
     */
    public void reassignTo(String newUserId) {
        if (newUserId == null || newUserId.isBlank()) {
            throw new InvalidDeviceTokenException("userId est obligatoire pour réassigner un device token");
        }
        this.userId = newUserId;
        this.updatedAt = Instant.now();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceToken that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
