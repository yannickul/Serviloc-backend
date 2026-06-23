package com.serviloc.notifications.domain.repository;

import com.serviloc.notifications.domain.model.NotificationLog;

/**
 * Port de persistance pour {@link NotificationLog} (implémenté en infrastructure/persistence).
 */
public interface NotificationLogRepository {

    NotificationLog save(NotificationLog notificationLog);

    PageResult<NotificationLog> findByUserId(String userId, PageQuery pageQuery);
}
