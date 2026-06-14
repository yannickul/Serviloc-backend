package com.serviloc.paiement.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommissionConfigJpaRepository
        extends JpaRepository<CommissionConfigJpaEntity, UUID> {

    // Retourne la première config (il n'y en a qu'une)
    java.util.Optional<CommissionConfigJpaEntity> findFirstByOrderByCreatedAtAsc();
}