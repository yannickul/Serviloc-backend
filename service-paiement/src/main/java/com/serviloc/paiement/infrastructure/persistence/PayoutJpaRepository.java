package com.serviloc.paiement.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayoutJpaRepository
        extends JpaRepository<PayoutJpaEntity, UUID> {

    Optional<PayoutJpaEntity> findByTransactionId(UUID transactionId);
    List<PayoutJpaEntity> findByProviderId(UUID providerId);
    List<PayoutJpaEntity> findAllByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
