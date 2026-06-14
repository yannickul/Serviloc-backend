package com.serviloc.paiement.infrastructure.persistence;

import com.serviloc.paiement.domain.model.CommissionConfig;
import com.serviloc.paiement.domain.repository.CommissionConfigRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CommissionConfigRepositoryAdapter implements CommissionConfigRepository {

    private final CommissionConfigJpaRepository jpa;

    public CommissionConfigRepositoryAdapter(CommissionConfigJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public CommissionConfig save(CommissionConfig config) {
        CommissionConfigJpaEntity entity = jpa.findById(config.getId())
                .orElse(new CommissionConfigJpaEntity(
                        config.getId(), config.getStandardRate(),
                        config.getUrgencyRate(), config.getCreatedAt()
                ));
        entity.setStandardRate(config.getStandardRate());
        entity.setUrgencyRate(config.getUrgencyRate());
        return toDomain(jpa.save(entity));
    }

    @Override
    public Optional<CommissionConfig> findFirst() {
        return jpa.findFirstByOrderByCreatedAtAsc().map(this::toDomain);
    }

    private CommissionConfig toDomain(CommissionConfigJpaEntity e) {
        try {
            var ctor = CommissionConfig.class.getDeclaredConstructor(
                    java.util.UUID.class, double.class, double.class,
                    java.time.LocalDateTime.class, java.time.LocalDateTime.class
            );
            ctor.setAccessible(true);
            return ctor.newInstance(
                    e.getId(), e.getStandardRate(), e.getUrgencyRate(),
                    e.getCreatedAt(), e.getUpdatedAt()
            );
        } catch (Exception ex) {
            throw new RuntimeException("Erreur reconstitution CommissionConfig", ex);
        }
    }
}