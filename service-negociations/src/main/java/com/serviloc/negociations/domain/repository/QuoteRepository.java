package com.serviloc.negociations.domain.repository;

import com.serviloc.negociations.domain.model.Quote;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuoteRepository {
    Quote save(Quote quote);
    Optional<Quote> findById(UUID id);
    Optional<Quote> findByDemandId(String demandId);
    Optional<Quote> findByDemandIdAndProviderId(String demandId, UUID providerId);
    List<Quote> findAllByDemandId(String demandId);
    List<Quote> findExpiredPending();
}