// adapter/rest/AgentLitigeController.java
package com.serviloc.litiges.adapter.rest;

import com.serviloc.litiges.application.dto.request.AgentSuspendUserRequest;
import com.serviloc.litiges.application.dto.request.ProposeResolutionRequest;
import com.serviloc.litiges.application.dto.request.SendMessageRequest;
import com.serviloc.litiges.application.dto.response.*;
import com.serviloc.litiges.application.port.in.AgentLitigeUseCase;
import com.serviloc.litiges.domain.model.LitigeStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur agent — routes /agent/litiges/** attendues par le Gateway et l'API_CONTRACT
 * (section 9). Absent avant cette correction : le Gateway routait vers ce service sans
 * qu'aucun contrôleur ne réponde, d'où des 404 systématiques.
 */
@RestController
@RequestMapping("/agent/litiges")
@RequiredArgsConstructor
@Tag(name = "Agent", description = "Traitement des litiges par l'agent assigné")
public class AgentLitigeController {

    private final AgentLitigeUseCase agentLitigeUseCase;

    @Operation(summary = "Lister mes litiges", description = "Litiges assignés à l'agent courant")
    @GetMapping
    public ResponseEntity<ApiResponse<LitigeListResponse>> getMyLitiges(
            @RequestHeader("X-User-Id") String agentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) LitigeStatus status) {

        return ResponseEntity.ok(ApiResponse.ok(agentLitigeUseCase.getMyLitiges(agentId, page, limit, status)));
    }

    @Operation(summary = "Détail d'un litige assigné")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LitigeDetailResponse>> getLitigeById(
            @RequestHeader("X-User-Id") String agentId,
            @PathVariable String id) {

        return ResponseEntity.ok(ApiResponse.ok(agentLitigeUseCase.getLitigeById(agentId, id)));
    }

    @Operation(summary = "Historique du litige", description = "Conversation de négociation + fil de messages litige")
    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<LitigeHistoryResponse>> getHistory(
            @RequestHeader("X-User-Id") String agentId,
            @PathVariable String id) {

        return ResponseEntity.ok(ApiResponse.ok(agentLitigeUseCase.getHistory(agentId, id)));
    }

    @Operation(summary = "Lister les messages du litige")
    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<List<LitigeMessageResponse>>> getMessages(
            @RequestHeader("X-User-Id") String agentId,
            @PathVariable String id) {

        return ResponseEntity.ok(ApiResponse.ok(agentLitigeUseCase.getMessages(agentId, id)));
    }

    @Operation(summary = "Envoyer un message au client/prestataire")
    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<LitigeMessageResponse>> sendMessage(
            @RequestHeader("X-User-Id") String agentId,
            @PathVariable String id,
            @Valid @RequestBody SendMessageRequest request) {

        var response = agentLitigeUseCase.sendMessage(agentId, id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @Operation(summary = "Proposer une résolution", description = "Émet litige.resolution_proposed — le litige reste EN_COURS tant que les deux parties n'ont pas validé")
    @PostMapping("/{id}/resolution")
    public ResponseEntity<ApiResponse<Void>> proposeResolution(
            @RequestHeader("X-User-Id") String agentId,
            @PathVariable String id,
            @Valid @RequestBody ProposeResolutionRequest request) {

        agentLitigeUseCase.proposeResolution(agentId, id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(null));
    }

    @Operation(summary = "Modifier la proposition de résolution", description = "Remet les acceptations client/prestataire à zéro")
    @PutMapping("/{id}/resolution")
    public ResponseEntity<ApiResponse<Void>> updateResolution(
            @RequestHeader("X-User-Id") String agentId,
            @PathVariable String id,
            @Valid @RequestBody ProposeResolutionRequest request) {

        agentLitigeUseCase.updateResolution(agentId, id, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "Clôturer le litige", description = "Déclenche le remboursement si applicable, passe le litige à RESOLU et émet litige.resolved")
    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<Void>> closeLitige(
            @RequestHeader("X-User-Id") String agentId,
            @PathVariable String id) {

        agentLitigeUseCase.closeLitige(agentId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "Suspendre un utilisateur", description = "Suspend le client ou le prestataire impliqué dans le litige")
    @PostMapping("/{id}/suspend-user")
    public ResponseEntity<ApiResponse<Void>> suspendUser(
            @RequestHeader("X-User-Id") String agentId,
            @PathVariable String id,
            @Valid @RequestBody AgentSuspendUserRequest request) {

        agentLitigeUseCase.suspendUser(agentId, id, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
