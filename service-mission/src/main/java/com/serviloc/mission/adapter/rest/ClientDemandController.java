// adapter/rest/ClientDemandController.java
package com.serviloc.mission.adapter.rest;

import com.serviloc.mission.application.dto.request.AcceptQuoteRequest;
import com.serviloc.mission.application.dto.request.CreateDemandRequest;
import com.serviloc.mission.application.dto.response.ApiResponse;
import com.serviloc.mission.application.dto.response.DemandResponse;
import com.serviloc.mission.application.dto.response.PagedResponse;
import com.serviloc.mission.application.port.in.DemandUseCase;
import com.serviloc.mission.domain.model.DemandStatus;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/client")
@Tag(name = "Client", description = "Endpoints accessibles par les clients : demandes, missions, évaluations")
@SecurityRequirement(name = "x-user-id")
@SecurityRequirement(name = "x-user-role")
public class ClientDemandController {

    private final DemandUseCase demandUseCase;

    public ClientDemandController(DemandUseCase demandUseCase) {
        this.demandUseCase = demandUseCase;
    }

    @PostMapping("/demands")
    public ResponseEntity<ApiResponse<DemandResponse>> createDemand(
            @Valid @RequestBody CreateDemandRequest request,
            @RequestHeader("X-User-Id") String clientId) {

        DemandResponse response = demandUseCase.createDemand(request, clientId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/demands")
    public ResponseEntity<PagedResponse<DemandResponse>> getDemands(
            @RequestHeader("X-User-Id") String clientId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {

        DemandStatus demandStatus = status != null
                ? DemandStatus.valueOf(status.toUpperCase()) : null;

        return ResponseEntity.ok(
                demandUseCase.getDemands(clientId, demandStatus, page, limit));
    }

    @GetMapping("/demands/{id}")
    public ResponseEntity<ApiResponse<DemandResponse>> getDemandById(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String clientId) {

        return ResponseEntity.ok(
                ApiResponse.success(demandUseCase.getDemandById(id, clientId)));
    }

    @DeleteMapping("/demands/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelDemand(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String clientId) {

        demandUseCase.cancelDemand(id, clientId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/demands/{id}/quote/accept")
    public ResponseEntity<ApiResponse<Void>> acceptQuote(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String clientId,
            @Valid @RequestBody AcceptQuoteRequest request) {

        demandUseCase.acceptQuote(id, clientId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/demands/{id}/quote/reject")
    public ResponseEntity<ApiResponse<Void>> rejectQuote(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String clientId) {

        demandUseCase.rejectQuote(id, clientId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}




















