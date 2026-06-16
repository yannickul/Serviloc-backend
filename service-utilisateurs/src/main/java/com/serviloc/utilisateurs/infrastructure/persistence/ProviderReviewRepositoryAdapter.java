package com.serviloc.utilisateurs.infrastructure.persistence;

import com.serviloc.utilisateurs.domain.model.ProviderReview;
import com.serviloc.utilisateurs.domain.repository.ProviderReviewRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ProviderReviewRepositoryAdapter implements ProviderReviewRepository {

    private final ProviderReviewJpaRepository jpa;

    public ProviderReviewRepositoryAdapter(ProviderReviewJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ProviderReview save(ProviderReview review) {
        ProviderReviewJpaEntity entity = new ProviderReviewJpaEntity(
                review.getId(), review.getAgentId(), review.getProviderId(),
                review.getVerdict(), review.getComment()
        );
        return toDomain(jpa.save(entity));
    }

    @Override
    public List<ProviderReview> findByProviderId(UUID providerId) {
        return jpa.findByProviderIdOrderByReviewedAtDesc(providerId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<ProviderReview> findLatestByProviderId(UUID providerId) {
        return jpa.findTopByProviderIdOrderByReviewedAtDesc(providerId)
                .map(this::toDomain);
    }

    @Override
    public boolean existsByAgentIdAndProviderId(UUID agentId, UUID providerId) {
        return jpa.existsByAgentIdAndProviderId(agentId, providerId);
    }

    private ProviderReview toDomain(ProviderReviewJpaEntity e) {
        try {
            var ctor = ProviderReview.class.getDeclaredConstructor(
                    UUID.class, UUID.class, UUID.class,
                    ProviderReview.Verdict.class, String.class,
                    java.time.LocalDateTime.class
            );
            ctor.setAccessible(true);
            return ctor.newInstance(
                    e.getId(), e.getAgentId(), e.getProviderId(),
                    e.getVerdict(), e.getComment(), e.getReviewedAt()
            );
        } catch (Exception ex) {
            throw new RuntimeException("Erreur reconstitution ProviderReview", ex);
        }
    }
}