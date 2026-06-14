package com.serviloc.paiement.domain.repository;

import com.serviloc.paiement.domain.model.Transaction;
import com.serviloc.paiement.domain.model.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(UUID id);
    Optional<Transaction> findByQuoteId(UUID quoteId);
    Optional<Transaction> findByDemandId(UUID demandId);
    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);
    double sumAmountByProviderIdAndCreatedAtBetween(UUID providerId,
                                                    LocalDateTime from,
                                                    LocalDateTime to);
    double sumCommissionBetween(LocalDateTime from, LocalDateTime to);
    long countByStatus(TransactionStatus status);
}