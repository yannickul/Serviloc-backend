package com.serviloc.notifications.application.service;

import com.serviloc.notifications.application.dto.NotificationLogView;
import com.serviloc.notifications.application.dto.PagedResult;
import com.serviloc.notifications.application.port.in.GetNotificationLogsUseCase;
import com.serviloc.notifications.domain.model.NotificationLog;
import com.serviloc.notifications.domain.repository.NotificationLogRepository;
import com.serviloc.notifications.domain.repository.PageQuery;
import com.serviloc.notifications.domain.repository.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificationLogQueryService implements GetNotificationLogsUseCase {

    private final NotificationLogRepository notificationLogRepository;

    public NotificationLogQueryService(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
    }

    @Override
    public PagedResult<NotificationLogView> getNotificationLogs(String userId, int page, int size) {
        PageResult<NotificationLog> result = notificationLogRepository.findByUserId(userId, PageQuery.of(page, size));

        return new PagedResult<>(
                result.content().stream().map(this::toView).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }

    private NotificationLogView toView(NotificationLog log) {
        return new NotificationLogView(log.getId(), log.getUserId(), log.getChannel(), log.getType(),
                log.getContent(), log.getStatus(), log.getSentAt());
    }
}
