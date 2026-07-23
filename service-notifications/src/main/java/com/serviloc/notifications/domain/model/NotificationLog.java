package com.serviloc.notifications.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Trace d'audit d'une notification envoyée (ou tentée) à un utilisateur.
 *
 * {@code type} correspond au nom de l'événement RabbitMQ d'origine (ex: "payment.confirmed"),
 * ce qui permet de tracer quel événement métier a déclenché quelle notification.
 */
public class NotificationLog {

    private final UUID id;
    private final String userId;
    private final NotificationChannel channel;
    private final String type;
    private final String content;
    private final NotificationStatus status;
    private final Instant sentAt;

    private NotificationLog(UUID id, String userId, NotificationChannel channel, String type,
                             String content, NotificationStatus status, Instant sentAt) {
        this.id = id;
        this.userId = userId;
        this.channel = channel;
        this.type = type;
        this.content = content;
        this.status = status;
        this.sentAt = sentAt;
    }

    /** Enregistre une notification envoyée avec succès. */
    public static NotificationLog sent(String userId, NotificationChannel channel, String type, String content) {
        return new NotificationLog(UUID.randomUUID(), userId, channel, type, content, NotificationStatus.SENT, Instant.now());
    }

    /** Enregistre une tentative de notification échouée (ex: provider SMS/FCM indisponible). */
    public static NotificationLog failed(String userId, NotificationChannel channel, String type, String content) {
        return new NotificationLog(UUID.randomUUID(), userId, channel, type, content, NotificationStatus.FAILED, Instant.now());
    }

    /** Reconstruction depuis la persistance. */
    public static NotificationLog reconstitute(UUID id, String userId, NotificationChannel channel, String type,
                                                String content, NotificationStatus status, Instant sentAt) {
        return new NotificationLog(id, userId, channel, type, content, status, sentAt);
    }

    public UUID getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationLog that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
