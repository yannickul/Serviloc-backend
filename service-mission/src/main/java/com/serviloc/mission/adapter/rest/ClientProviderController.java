// adapter/rest/ClientProviderController.java
package com.serviloc.mission.adapter.rest;

import com.serviloc.mission.application.dto.response.ApiResponse;
import com.serviloc.mission.application.port.in.ProviderSearchUseCase;
import com.serviloc.mission.infrastructure.external.ProviderSummary;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/client")
public class ClientProviderController {

    private final ProviderSearchUseCase providerSearchUseCase;

    public ClientProviderController(ProviderSearchUseCase providerSearchUseCase) {
        this.providerSearchUseCase = providerSearchUseCase;
    }

    @GetMapping("/providers/search")
    public ResponseEntity<ApiResponse<List<ProviderSummary>>> searchProviders(
            @RequestHeader("X-User-Id") String clientId,
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "20") int radiusKm,
            @RequestParam(required = false) String categoryId) {

        List<ProviderSummary> providers =
                providerSearchUseCase.searchProviders(lat, lng, radiusKm, categoryId);

        return ResponseEntity.ok(ApiResponse.success(providers));
    }
}