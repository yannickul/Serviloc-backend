package com.serviloc.notifications.presentation.dto;

import com.serviloc.notifications.application.dto.NotificationLogView;
import com.serviloc.notifications.domain.model.NotificationChannel;
import com.serviloc.notifications.domain.model.NotificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Entrée d'audit d'une notification envoyée")
public record NotificationLogResponse(
        UUID id,
        String userId,
        NotificationChannel channel,
        String type,
        String content,
        NotificationStatus status,
        Instant sentAt
) {
    public static NotificationLogResponse from(NotificationLogView view) {
        return new NotificationLogResponse(view.id(), view.userId(), view.channel(), view.type(),
                view.content(), view.status(), view.sentAt());
    }
}
