package com.serviloc.mission.domain.repository;

import com.serviloc.mission.domain.model.ProviderApplication;

import java.util.List;
import java.util.Optional;

public interface ProviderApllicationRepository {
    ProviderApplication save(ProviderApplication application);
    Optional<ProviderApplication> findByDemandIdAndProviderId(String demandId, String providerId);
    List<ProviderApplication> findByDemandId(String demandId);
}
