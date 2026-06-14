package com.serviloc.paiement.infrastructure.persistence;

import com.serviloc.paiement.domain.model.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionJpaRepository
        extends JpaRepository<TransactionJpaEntity, UUID> {

    Optional<TransactionJpaEntity> findByQuoteId(UUID quoteId);
    Optional<TransactionJpaEntity> findByDemandId(UUID demandId);
    Page<TransactionJpaEntity> findByStatus(TransactionStatus status, Pageable pageable);
    long countByStatus(TransactionStatus status);

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) FROM TransactionJpaEntity t
        WHERE t.providerId = :providerId
        AND t.status = 'LIBERE'
        AND t.createdAt BETWEEN :from AND :to
        """)
    double sumAmountByProviderIdAndCreatedAtBetween(
            @Param("providerId") UUID providerId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        SELECT COALESCE(SUM(t.commissionAmount), 0) FROM TransactionJpaEntity t
        WHERE t.status = 'LIBERE'
        AND t.createdAt BETWEEN :from AND :to
        """)
    double sumCommissionBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}