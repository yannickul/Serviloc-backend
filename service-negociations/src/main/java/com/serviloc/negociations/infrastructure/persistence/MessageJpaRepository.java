package com.serviloc.negociations.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageJpaRepository
        extends JpaRepository<MessageJpaEntity, UUID> {

    Page<MessageJpaEntity> findByConversationIdOrderBySentAtDesc(
            UUID conversationId, Pageable pageable);
}