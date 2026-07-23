package com.serviloc.paiement.infrastructure.persistence;

import com.serviloc.paiement.domain.model.Transaction;
import com.serviloc.paiement.domain.model.TransactionStatus;
import com.serviloc.paiement.domain.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Component
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final TransactionJpaRepository jpa;

    public TransactionRepositoryAdapter(TransactionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Transaction save(Transaction t) {
        TransactionJpaEntity entity = jpa.findById(t.getId())
                .orElse(new TransactionJpaEntity(
                        t.getId(), t.getReference(), t.getDemandId(), t.getMissionId(),
                        t.getClientId(), t.getProviderId(),
                        t.getQuoteId(), t.getAmount(), t.getCommissionRate(),
                        t.getCommissionAmount(), t.getNetAmount(),
                        t.getStatus(), t.getPaymentMethod(), t.getPhoneNumber()
                ));
        entity.setStatus(t.getStatus());
        entity.setExternalRef(t.getExternalRef());
        entity.setMissionId(t.getMissionId());
        return toDomain(jpa.save(entity));
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Transaction> findByQuoteId(UUID quoteId) {
        return jpa.findByQuoteId(quoteId).map(this::toDomain);
    }

    @Override
    public Optional<Transaction> findByDemandId(UUID demandId) {
        return jpa.findByDemandId(demandId).map(this::toDomain);
    }

    @Override
    public Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable) {
        return jpa.findByStatus(status, pageable).map(this::toDomain);
    }

    @Override
    public List<Transaction> findAllByCreatedAtBetween(LocalDateTime from, LocalDateTime to) {
        return jpa.findAllByCreatedAtBetween(from, to).stream().map(this::toDomain).toList();
    }

    @Override
    public double sumAmountByProviderIdAndCreatedAtBetween(UUID providerId,
                                                           LocalDateTime from,
                                                           LocalDateTime to) {
        return jpa.sumAmountByProviderIdAndCreatedAtBetween(providerId, from, to);
    }

    @Override
    public double sumCommissionBetween(LocalDateTime from, LocalDateTime to) {
        return jpa.sumCommissionBetween(from, to);
    }

    @Override
    public long countByStatus(TransactionStatus status) {
        return jpa.countByStatus(status);
    }

    @Override
    public List<Transaction> findByClientIdAndStatus(UUID clientId, TransactionStatus status) {
        return jpa.findByClientIdAndStatus(clientId, status)
                .stream().map(this::toDomain).toList();
    }

    private Transaction toDomain(TransactionJpaEntity e) {
        return new Transaction(
                e.getId(), e.getReference(), e.getDemandId(), e.getMissionId(),
                e.getClientId(), e.getProviderId(),
                e.getQuoteId(), e.getAmount(), e.getCommissionRate(),
                e.getCommissionAmount(), e.getNetAmount(), e.getStatus(),
                e.getPaymentMethod(), e.getPhoneNumber(), e.getExternalRef(),
                e.getCreatedAt(), e.getUpdatedAt()
        );
    }

    @Override
    public double sumAmountByClientIdAndStatus(UUID clientId, TransactionStatus status) {
        return jpa.sumAmountByClientIdAndStatus(clientId, status);
    }
    @Override
    public double sumCommissionAmountBetween(LocalDateTime from, LocalDateTime to) {
        return jpa.sumCommissionAmountBetween(from, to);
    }

    @Override
    public double sumSequesteredAmountBetween(LocalDateTime from, LocalDateTime to) {
        return jpa.sumSequesteredAmountBetween(from, to);
    }
}
