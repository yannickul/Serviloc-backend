package com.serviloc.utilisateurs.adapter.rest;

import com.serviloc.utilisateurs.application.dto.ProfileDtos.ProviderSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints internes — appelés par les autres microservices via Feign.
 * STUB S1 — implémentation complète en S2.
 */
@RestController
@RequestMapping("/internal")
@Tag(name = "Internal", description = "Endpoints inter-services")
public class InternalController {

    @GetMapping("/providers")
    @Operation(summary = "[STUB S1] Liste des prestataires disponibles")
    public ResponseEntity<List<ProviderSummary>> getProviders(
            @RequestParam(required = false, defaultValue = "0") double lat,
            @RequestParam(required = false, defaultValue = "0") double lng,
            @RequestParam(required = false, defaultValue = "10") double radiusKm,
            @RequestParam(required = false) String specialty) {
        // Retourne liste vide en S1 — filtre géo Haversine en S2
        return ResponseEntity.ok(List.of());
    }
}