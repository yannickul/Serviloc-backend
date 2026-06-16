package com.serviloc.utilisateurs.domain.repository;

import com.serviloc.utilisateurs.domain.model.ProviderReview;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProviderReviewRepository {
    ProviderReview save(ProviderReview review);
    List<ProviderReview> findByProviderId(UUID providerId);
    Optional<ProviderReview> findLatestByProviderId(UUID providerId);
    boolean existsByAgentIdAndProviderId(UUID agentId, UUID providerId);
}