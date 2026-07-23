package com.serviloc.notifications.infrastructure.persistence;

import com.serviloc.notifications.domain.model.NotificationChannel;
import com.serviloc.notifications.domain.model.NotificationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_logs", indexes = {
        @Index(name = "idx_notification_logs_user_id", columnList = "user_id")
})
public class NotificationLogJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private NotificationChannel channel;

    @Column(nullable = false, length = 64)
    private String type;

    @Lob
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private NotificationStatus status;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    protected NotificationLogJpaEntity() {
        // requis par JPA
    }

    public NotificationLogJpaEntity(UUID id, String userId, NotificationChannel channel, String type,
                                     String content, NotificationStatus status, Instant sentAt) {
        this.id = id;
        this.userId = userId;
        this.channel = channel;
        this.type = type;
        this.content = content;
        this.status = status;
        this.sentAt = sentAt;
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
}
