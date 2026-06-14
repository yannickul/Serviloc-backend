package com.serviloc.paiement.infrastructure.persistence;

import com.serviloc.paiement.domain.model.Payout;
import com.serviloc.paiement.domain.repository.PayoutRepository;
import org.springframework.stereotype.Component;

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
                        p.getId(), p.getTransactionId(), p.getProviderId(),
                        p.getAmount(), p.getCommissionAmount(), p.getStatus()
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

    private Payout toDomain(PayoutJpaEntity e) {
        try {
            var ctor = Payout.class.getDeclaredConstructor(
                    UUID.class, UUID.class, UUID.class, double.class, double.class,
                    Payout.PayoutStatus.class, String.class,
                    java.time.LocalDateTime.class, java.time.LocalDateTime.class
            );
            ctor.setAccessible(true);
            return ctor.newInstance(
                    e.getId(), e.getTransactionId(), e.getProviderId(),
                    e.getAmount(), e.getCommissionAmount(), e.getStatus(),
                    e.getExternalRef(), e.getCreatedAt(), e.getUpdatedAt()
            );
        } catch (Exception ex) {
            throw new RuntimeException("Erreur reconstitution Payout", ex);
        }
    }
}