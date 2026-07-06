// adapter/rest/InternalLitigeController.java
package com.serviloc.litiges.adapter.rest;

import com.serviloc.litiges.application.dto.request.CreateLitigeRequest;
import com.serviloc.litiges.application.dto.response.ApiResponse;
import com.serviloc.litiges.application.dto.response.LitigeResponse;
import com.serviloc.litiges.application.port.in.LitigeUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/litiges")
@RequiredArgsConstructor
@Tag(name = "Internal", description = "Endpoints réservés aux appels inter-services (Service Missions)")
public class InternalLitigeController {

    private final LitigeUseCase litigeUseCase;

    @Value("${serviloc.internal-token}")
    private String expectedToken;

    @Operation(summary = "Créer un litige", description = "Appelé par Service Missions lors d'un signalement client ou prestataire")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Litige créé"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Header X-Internal-Token manquant"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Token invalide"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Litige déjà ouvert pour cette mission")
    })
    @PostMapping
    public ResponseEntity<?> createLitige(
            @RequestHeader("X-Internal-Token") String token,
            @Valid @RequestBody CreateLitigeRequest request) {

        if (!expectedToken.equals(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("FORBIDDEN", "Token inter-services invalide"));
        }

        LitigeResponse response = litigeUseCase.createLitige(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @Operation(summary = "Lire un litige par ID", description = "Lecture inter-services — contrôle depuis Service Missions")
    @GetMapping("/{id}")
    public ResponseEntity<?> getLitigeById(
            @RequestHeader("X-Internal-Token") String token,
            @PathVariable String id) {

        if (!expectedToken.equals(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("FORBIDDEN", "Token inter-services invalide"));
        }

        LitigeResponse response = litigeUseCase.getLitigeById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}