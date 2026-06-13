package com.serviloc.negociations.domain.repository;

import com.serviloc.negociations.domain.model.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository {
    Conversation save(Conversation conversation);
    Optional<Conversation> findById(UUID id);
    Optional<Conversation> findByClientIdAndProviderIdAndDemandId(
            UUID clientId, UUID providerId, UUID demandId);
    Page<Conversation> findByClientIdOrderByLastMessageAtDesc(UUID clientId, Pageable pageable);
    Page<Conversation> findByProviderIdOrderByLastMessageAtDesc(UUID providerId, Pageable pageable);
    Optional<Conversation> findByDemandId(UUID demandId);
}