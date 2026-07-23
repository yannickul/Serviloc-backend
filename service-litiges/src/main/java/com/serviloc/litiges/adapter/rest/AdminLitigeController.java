// adapter/rest/AdminLitigeController.java
package com.serviloc.litiges.adapter.rest;

import com.serviloc.litiges.application.dto.request.AssignLitigeRequest;
import com.serviloc.litiges.application.dto.request.ReassignLitigeRequest;
import com.serviloc.litiges.application.dto.response.ApiResponse;
import com.serviloc.litiges.application.dto.response.LitigeDetailResponse;
import com.serviloc.litiges.application.dto.response.LitigeListResponse;
import com.serviloc.litiges.application.dto.response.StatsResponse;
import com.serviloc.litiges.application.port.in.AdminLitigeUseCase;
import com.serviloc.litiges.application.service.AdminLitigeService;
import com.serviloc.litiges.domain.model.LitigeStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/litiges")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Gestion des litiges — accès admin")
public class AdminLitigeController {

    private final AdminLitigeUseCase adminLitigeUseCase;
    private final AdminLitigeService adminLitigeService; // pour le calcul du meta de pagination

    @Operation(summary = "Lister les litiges", description = "Liste paginée + metrics, filtrable par status et agentId")
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
        var meta = adminLitigeService.getPageMeta(page, limit, status, agentId);
        return ResponseEntity.ok(ApiResponse.ok(response, meta));
    }

    @Operation(summary = "Détail d'un litige", description = "Litige agrégé avec conversation et profils client/provider/agent")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Litige trouvé"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Litige introuvable")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LitigeDetailResponse>> getLitigeById(
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id) {

        LitigeDetailResponse response = adminLitigeUseCase.getLitigeById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Statistiques litiges", description = "Compteurs globaux pour le dashboard admin")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<StatsResponse>> getStats(
            @RequestHeader("X-User-Role") String userRole) {
        return ResponseEntity.ok(ApiResponse.ok(adminLitigeUseCase.getStats()));
    }

    @Operation(summary = "Assigner un agent", description = "Première assignation — passe le litige de OUVERT à EN_COURS")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Agent assigné"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Litige introuvable"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Litige non OUVERT")
    })
    @PostMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<com.serviloc.litiges.application.dto.response.AssignResponse>> assignLitige(
            @RequestHeader("X-User-Role") String userRole,
            @PathVariable String id,
            @Valid @RequestBody AssignLitigeRequest request) {

        var response = adminLitigeUseCase.assignLitige(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Réassigner un agent", description = "Change l'agent d'un litige déjà EN_COURS")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Agent réassigné"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Litige introuvable"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Litige non EN_COURS")
    })
    @PutMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<com.serviloc.litiges.application.dto.response.AssignResponse>> reassignLitige(
            @RequestHeader("X-User-Role") String userRole,
            @PathVariable String id,
            @Valid @RequestBody ReassignLitigeRequest request) {

        var response = adminLitigeUseCase.reassignLitige(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
