package com.serviloc.negociations.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationJpaRepository
        extends JpaRepository<ConversationJpaEntity, UUID> {

    Optional<ConversationJpaEntity> findByClientIdAndProviderIdAndDemandId(
            UUID clientId, UUID providerId, UUID demandId);

    Page<ConversationJpaEntity> findByClientIdOrderByLastMessageAtDesc(
            UUID clientId, Pageable pageable);

    Page<ConversationJpaEntity> findByProviderIdOrderByLastMessageAtDesc(
            UUID providerId, Pageable pageable);

    Optional<ConversationJpaEntity> findByDemandId(UUID demandId);
}