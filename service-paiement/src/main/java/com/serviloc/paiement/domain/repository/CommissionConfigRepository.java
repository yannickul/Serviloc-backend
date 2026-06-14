package com.serviloc.paiement.domain.repository;

import com.serviloc.paiement.domain.model.CommissionConfig;
import java.util.Optional;

public interface CommissionConfigRepository {
    CommissionConfig save(CommissionConfig config);
    Optional<CommissionConfig> findFirst();
}