package com.serviloc.notifications.application.port.in;

import com.serviloc.notifications.application.dto.NotificationLogView;
import com.serviloc.notifications.application.dto.PagedResult;

/**
 * Use case : audit des notifications envoyées à un utilisateur.
 * Correspond à {@code GET /internal/notification-logs/:userId}.
 */
public interface GetNotificationLogsUseCase {

    PagedResult<NotificationLogView> getNotificationLogs(String userId, int page, int size);
}
