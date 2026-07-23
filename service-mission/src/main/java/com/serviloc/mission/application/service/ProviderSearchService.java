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
        // radiusKm est dans la signature de l'interface mais UtilisateurClientAdapter
        // utilise un rayon fixe de 20km défini dans l'appel Feign (accord Sprint 1).
        // On passe categoryId comme specialty — voir point 1.3 du CONTEXT.md section 15.
        return providerPort.searchProviders(lat, lng, categoryId);
    }
}