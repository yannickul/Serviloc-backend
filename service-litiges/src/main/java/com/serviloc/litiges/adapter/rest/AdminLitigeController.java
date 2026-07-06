// adapter/rest/AdminLitigeController.java
package com.serviloc.litiges.adapter.rest;

import com.serviloc.litiges.application.dto.request.AssignLitigeRequest;
import com.serviloc.litiges.application.dto.request.ResolveRequest;
import com.serviloc.litiges.application.dto.response.ApiResponse;
import com.serviloc.litiges.application.dto.response.LitigeDetailResponse;
import com.serviloc.litiges.application.dto.response.LitigeListResponse;
import com.serviloc.litiges.application.port.in.AdminLitigeUseCase;
import com.serviloc.litiges.domain.model.LitigeStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/litiges")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Gestion des litiges — accès admin et agents service client")
public class AdminLitigeController {

    private final AdminLitigeUseCase adminLitigeUseCase;

    @Operation(summary = "Lister les litiges", description = "Liste paginée, filtrable par status et agentId")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Liste retournée"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Header X-User-Role manquant")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<LitigeListResponse>> getLitiges(
            @RequestHeader("X-User-Role") String userRole,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) LitigeStatus status,
            @RequestParam(required = false) String agentId) {

        LitigeListResponse response = adminLitigeUseCase.getLitiges(page, limit, status, agentId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Détail d'un litige", description = "Retourne le litige agrégé avec l'historique du chat")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Litige trouvé"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Litige introuvable"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Header manquant")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LitigeDetailResponse>> getLitigeById(
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id) {

        LitigeDetailResponse response = adminLitigeUseCase.getLitigeById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Assigner un agent", description = "Passe le litige de OUVERT à EN_COURS")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Agent assigné"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Litige introuvable"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Litige non OUVERT")
    })
    @PostMapping("/{id}/assign")
    public ResponseEntity<Void> assignLitige(
            @RequestHeader("X-User-Role") String userRole,
            @PathVariable String id,
            @Valid @RequestBody AssignLitigeRequest request) {

        adminLitigeUseCase.assignLitige(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Résoudre un litige", description = "Enregistre la résolution et déclenche le remboursement si applicable")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Litige résolu"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Litige introuvable"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Litige non EN_COURS ou montant invalide"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Service Paiement indisponible")
    })
    @PostMapping("/{id}/resolve")
    public ResponseEntity<Void> resolveLitige(
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader("X-User-Id") String agentId,
            @PathVariable String id,
            @Valid @RequestBody ResolveRequest request) {

        adminLitigeUseCase.resolveLitige(id, request, agentId);
        return ResponseEntity.noContent().build();
    }
}