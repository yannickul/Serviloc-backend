package com.serviloc.notifications.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationLogJpaRepository extends JpaRepository<NotificationLogJpaEntity, UUID> {

    Page<NotificationLogJpaEntity> findByUserId(String userId, Pageable pageable);
}
