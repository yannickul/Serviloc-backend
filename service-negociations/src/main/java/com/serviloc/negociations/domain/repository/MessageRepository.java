package com.serviloc.negociations.domain.repository;

import com.serviloc.negociations.domain.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MessageRepository {
    Message save(Message message);
    Page<Message> findByConversationIdOrderBySentAtDesc(UUID conversationId, Pageable pageable);
}