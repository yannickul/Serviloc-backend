package com.serviloc.paiement.domain.repository;

import com.serviloc.paiement.domain.model.Payout;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayoutRepository {
    Payout save(Payout payout);
    Optional<Payout> findByTransactionId(UUID transactionId);
    List<Payout> findByProviderId(UUID providerId);
}