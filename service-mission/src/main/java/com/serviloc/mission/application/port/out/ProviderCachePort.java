// application/port/out/ProviderCachePort.java
package com.serviloc.mission.application.port.out;

import com.serviloc.mission.infrastructure.external.ProviderSummary;
import java.util.List;

public interface ProviderCachePort {
    List<ProviderSummary> getCachedProviders(String cacheKey);
    void cacheProviders(String cacheKey, List<ProviderSummary> providers);
}