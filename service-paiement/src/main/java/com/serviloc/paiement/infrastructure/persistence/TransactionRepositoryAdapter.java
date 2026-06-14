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
                        t.getId(), t.getDemandId(), t.getClientId(), t.getProviderId(),
                        t.getQuoteId(), t.getAmount(), t.getCommissionRate(),
                        t.getCommissionAmount(), t.getNetAmount(),
                        t.getStatus(), t.getPaymentMethod(), t.getPhoneNumber()
                ));
        entity.setStatus(t.getStatus());
        entity.setExternalRef(t.getExternalRef());
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

    private Transaction toDomain(TransactionJpaEntity e) {
        try {
            var ctor = Transaction.class.getDeclaredConstructor(
                    UUID.class, UUID.class, UUID.class, UUID.class, UUID.class,
                    double.class, double.class, double.class, double.class,
                    TransactionStatus.class, String.class, String.class, String.class,
                    LocalDateTime.class, LocalDateTime.class
            );
            ctor.setAccessible(true);
            return ctor.newInstance(
                    e.getId(), e.getDemandId(), e.getClientId(), e.getProviderId(),
                    e.getQuoteId(), e.getAmount(), e.getCommissionRate(),
                    e.getCommissionAmount(), e.getNetAmount(), e.getStatus(),
                    e.getPaymentMethod(), e.getPhoneNumber(), e.getExternalRef(),
                    e.getCreatedAt(), e.getUpdatedAt()
            );
        } catch (Exception ex) {
            throw new RuntimeException("Erreur reconstitution Transaction", ex);
        }
    }
}