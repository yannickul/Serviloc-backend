// UtilisateurClient.java
package com.serviloc.mission.infrastructure.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "service-utilisateurs",
        fallback = UtilisateurClientFallback.class
)
public interface UtilisateurClient {

    @GetMapping("/internal/providers")
    List<ProviderSummary> getProviders(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam int radiusKm,
            @RequestParam String specialty
    );

    @GetMapping("/internal/users/{id}")
    UserSummary getUserById(@PathVariable String id);

    @PutMapping("/internal/users/{id}/rating")
    void updateRating(@PathVariable String id, @RequestBody UpdateRatingRequest request);
}