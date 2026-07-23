package com.serviloc.negociations.domain.repository;

import com.serviloc.negociations.domain.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface MessageRepository {
    Message save(Message message);
    Optional<Message> findById(UUID id);
    Page<Message> findByConversationIdOrderBySentAtDesc(UUID conversationId, Pageable pageable);
    Optional<Message> findLastMessage(UUID conversationId);
}