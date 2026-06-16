// adapter/rest/ClientDemandController.java
package com.serviloc.mission.adapter.rest;

import com.serviloc.mission.application.dto.request.CreateDemandRequest;
import com.serviloc.mission.application.dto.response.ApiResponse;
import com.serviloc.mission.application.dto.response.DemandResponse;
import com.serviloc.mission.application.dto.response.PagedResponse;
import com.serviloc.mission.application.port.in.DemandUseCase;
import com.serviloc.mission.domain.model.DemandStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/client")
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
}




















