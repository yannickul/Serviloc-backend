// application/service/ProviderSearchService.java
package com.serviloc.mission.application.service;

import com.serviloc.mission.application.port.in.ProviderSearchUseCase;
import com.serviloc.mission.application.port.out.ProviderPort;
import com.serviloc.mission.infrastructure.external.ProviderSummary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProviderSearchService implements ProviderSearchUseCase {

    private final ProviderPort providerPort;

    public ProviderSearchService(ProviderPort providerPort) {
        this.providerPort = providerPort;
    }

    @Override
    public List<ProviderSummary> searchProviders(
            double lat, double lng, int radiusKm, String categoryId) {
        return providerPort.searchProviders(lat, lng, categoryId);
    }
}