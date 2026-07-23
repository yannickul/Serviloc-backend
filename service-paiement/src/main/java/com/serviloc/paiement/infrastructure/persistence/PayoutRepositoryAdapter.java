package com.serviloc.paiement.infrastructure.persistence;

import com.serviloc.paiement.domain.model.Payout;
import com.serviloc.paiement.domain.repository.PayoutRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PayoutRepositoryAdapter implements PayoutRepository {

    private final PayoutJpaRepository jpa;

    public PayoutRepositoryAdapter(PayoutJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Payout save(Payout p) {
        PayoutJpaEntity entity = jpa.findById(p.getId())
                .orElse(new PayoutJpaEntity(
                        p.getId(), p.getReference(), p.getTransactionId(), p.getMissionId(),
                        p.getProviderId(), p.getAmount(), p.getCommissionAmount(),
                        p.getNetAmount(), p.getStatus()
                ));
        entity.setStatus(p.getStatus());
        entity.setExternalRef(p.getExternalRef());
        return toDomain(jpa.save(entity));
    }

    @Override
    public Optional<Payout> findByTransactionId(UUID transactionId) {
        return jpa.findByTransactionId(transactionId).map(this::toDomain);
    }

    @Override
    public List<Payout> findByProviderId(UUID providerId) {
        return jpa.findByProviderId(providerId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Payout> findAllByCreatedAtBetween(LocalDateTime from, LocalDateTime to) {
        return jpa.findAllByCreatedAtBetween(from, to).stream().map(this::toDomain).toList();
    }

    private Payout toDomain(PayoutJpaEntity e) {
        return new Payout(
                e.getId(), e.getReference(), e.getTransactionId(), e.getMissionId(), e.getProviderId(),
                e.getAmount(), e.getCommissionAmount(), e.getNetAmount(), e.getStatus(),
                e.getExternalRef(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
