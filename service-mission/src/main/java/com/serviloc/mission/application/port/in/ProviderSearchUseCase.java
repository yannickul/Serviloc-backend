// application/port/in/ProviderSearchUseCase.java
package com.serviloc.mission.application.port.in;

import com.serviloc.mission.infrastructure.external.ProviderSummary;
import java.util.List;

public interface ProviderSearchUseCase {
    List<ProviderSummary> searchProviders(double lat, double lng, int radiusKm, String categoryId);
}