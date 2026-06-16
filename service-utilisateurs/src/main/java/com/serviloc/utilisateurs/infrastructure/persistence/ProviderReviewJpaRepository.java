package com.serviloc.utilisateurs.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProviderReviewJpaRepository
        extends JpaRepository<ProviderReviewJpaEntity, UUID> {

    List<ProviderReviewJpaEntity> findByProviderIdOrderByReviewedAtDesc(UUID providerId);
    Optional<ProviderReviewJpaEntity> findTopByProviderIdOrderByReviewedAtDesc(UUID providerId);
    boolean existsByAgentIdAndProviderId(UUID agentId, UUID providerId);
}