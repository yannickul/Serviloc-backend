package com.serviloc.notifications.infrastructure.persistence;

import com.serviloc.notifications.domain.model.NotificationLog;
import com.serviloc.notifications.domain.repository.NotificationLogRepository;
import com.serviloc.notifications.domain.repository.PageQuery;
import com.serviloc.notifications.domain.repository.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class NotificationLogRepositoryAdapter implements NotificationLogRepository {

    private final NotificationLogJpaRepository jpaRepository;

    public NotificationLogRepositoryAdapter(NotificationLogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public NotificationLog save(NotificationLog notificationLog) {
        NotificationLogJpaEntity entity = new NotificationLogJpaEntity(
                notificationLog.getId(),
                notificationLog.getUserId(),
                notificationLog.getChannel(),
                notificationLog.getType(),
                notificationLog.getContent(),
                notificationLog.getStatus(),
                notificationLog.getSentAt());
        NotificationLogJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public PageResult<NotificationLog> findByUserId(String userId, PageQuery pageQuery) {
        Pageable pageable = PageRequest.of(pageQuery.page(), pageQuery.size(), Sort.by("sentAt").descending());
        Page<NotificationLogJpaEntity> page = jpaRepository.findByUserId(userId, pageable);
        return new PageResult<>(
                page.getContent().stream().map(this::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements());
    }

    private NotificationLog toDomain(NotificationLogJpaEntity entity) {
        return NotificationLog.reconstitute(entity.getId(), entity.getUserId(), entity.getChannel(),
                entity.getType(), entity.getContent(), entity.getStatus(), entity.getSentAt());
    }
}
