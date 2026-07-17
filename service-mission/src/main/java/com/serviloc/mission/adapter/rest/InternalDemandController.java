// adapter/rest/InternalDemandController.java
package com.serviloc.mission.adapter.rest;

import com.serviloc.mission.application.dto.response.ApiResponse;
import com.serviloc.mission.application.dto.response.InternalDemandResponse;
import com.serviloc.mission.application.service.DemandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal")
@Tag(name = "Internal", description = "Endpoints inter-services — non exposés au Gateway")
public class InternalDemandController {

    private final DemandService demandService;

    public InternalDemandController(DemandService demandService) {
        this.demandService = demandService;
    }

    @Operation(
            summary = "Description courte d'une demande",
            description = "Utilisé par Service Utilisateurs pour construire le champ missionLabel du profil client."
    )
    @GetMapping("/demands/{demandId}")
    public ResponseEntity<ApiResponse<InternalDemandResponse>> getDemand(
            @Parameter(hidden = true) @RequestHeader("X-Internal-Token") String internalToken,
            @PathVariable String demandId) {

        return ResponseEntity.ok(ApiResponse.success(demandService.getDemandForInternal(demandId)));
    }
}