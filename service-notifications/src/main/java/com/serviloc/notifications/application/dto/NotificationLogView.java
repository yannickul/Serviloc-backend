package com.serviloc.notifications.application.dto;

import com.serviloc.notifications.domain.model.NotificationChannel;
import com.serviloc.notifications.domain.model.NotificationStatus;

import java.time.Instant;
import java.util.UUID;

public record NotificationLogView(UUID id, String userId, NotificationChannel channel, String type,
                                    String content, NotificationStatus status, Instant sentAt) {
}
