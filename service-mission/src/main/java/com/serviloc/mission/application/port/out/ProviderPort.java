// application/port/out/ProviderPort.java
package com.serviloc.mission.application.port.out;

import com.serviloc.mission.infrastructure.external.ProviderSummary;
import java.util.List;

public interface ProviderPort {
    List<ProviderSummary> searchProviders(double lat, double lng, String categoryId);
}