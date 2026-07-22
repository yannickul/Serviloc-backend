// adapter/rest/ClientProviderLitigeController.java
package com.serviloc.litiges.adapter.rest;

import com.serviloc.litiges.application.dto.request.RejectResolutionRequest;
import com.serviloc.litiges.application.dto.response.ApiResponse;
import com.serviloc.litiges.application.port.in.ClientProviderLitigeUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleurs client et prestataire — acceptation/refus de la résolution proposée
 * par l'agent (routes /client/litiges/** et /provider/litiges/**, absentes avant
 * cette correction).
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Client / Provider", description = "Acceptation ou refus d'une résolution de litige")
public class ClientProviderLitigeController {

    private final ClientProviderLitigeUseCase clientProviderLitigeUseCase;

    @Operation(summary = "Accepter la résolution proposée (client)")
    @PatchMapping("/client/litiges/{id}/resolution/accept")
    public ResponseEntity<ApiResponse<Void>> acceptAsClient(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id) {
        clientProviderLitigeUseCase.acceptResolution(id, userId, "CLIENT");
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "Refuser la résolution proposée (client)")
    @PatchMapping("/client/litiges/{id}/resolution/reject")
    public ResponseEntity<ApiResponse<Void>> rejectAsClient(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id,
            @RequestBody(required = false) RejectResolutionRequest request) {
        String reason = request != null ? request.reason() : null;
        clientProviderLitigeUseCase.rejectResolution(id, userId, "CLIENT", reason);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "Accepter la résolution proposée (prestataire)")
    @PatchMapping("/provider/litiges/{id}/resolution/accept")
    public ResponseEntity<ApiResponse<Void>> acceptAsProvider(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id) {
        clientProviderLitigeUseCase.acceptResolution(id, userId, "PROVIDER");
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "Refuser la résolution proposée (prestataire)")
    @PatchMapping("/provider/litiges/{id}/resolution/reject")
    public ResponseEntity<ApiResponse<Void>> rejectAsProvider(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id,
            @RequestBody(required = false) RejectResolutionRequest request) {
        String reason = request != null ? request.reason() : null;
        clientProviderLitigeUseCase.rejectResolution(id, userId, "PROVIDER", reason);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
