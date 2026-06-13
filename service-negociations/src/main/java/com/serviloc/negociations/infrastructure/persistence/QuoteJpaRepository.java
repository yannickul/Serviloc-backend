package com.serviloc.negociations.infrastructure.persistence;

import com.serviloc.negociations.domain.model.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuoteJpaRepository
        extends JpaRepository<QuoteJpaEntity, UUID> {

    Optional<QuoteJpaEntity> findByDemandId(UUID demandId);

    @Query("SELECT q FROM QuoteJpaEntity q WHERE q.status = :status AND q.expiresAt < :now")
    List<QuoteJpaEntity> findExpiredByStatus(QuoteStatus status, LocalDateTime now);
}