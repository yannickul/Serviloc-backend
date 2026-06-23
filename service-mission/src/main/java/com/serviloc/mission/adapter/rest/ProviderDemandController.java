// adapter/rest/ProviderDemandController.java
package com.serviloc.mission.adapter.rest;

import com.serviloc.mission.application.dto.response.ApiResponse;
import com.serviloc.mission.application.dto.response.DemandResponse;
import com.serviloc.mission.application.dto.response.MissionResponse;
import com.serviloc.mission.application.port.in.DemandUseCase;
import com.serviloc.mission.application.port.in.MissionUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/provider")
public class ProviderDemandController {

    private final DemandUseCase demandUseCase;
    private final MissionUseCase missionUseCase;

    public ProviderDemandController(
            DemandUseCase demandUseCase,
            MissionUseCase missionUseCase) {
        this.demandUseCase = demandUseCase;
        this.missionUseCase = missionUseCase;
    }

    @GetMapping("/demands")
    public ResponseEntity<ApiResponse<List<DemandResponse>>> getAvailableDemands(
            @RequestHeader("X-User-Id") String providerId,
            @RequestParam(required = false) String categoryId) {

        return ResponseEntity.ok(
                ApiResponse.success(demandUseCase.getOpenDemands(categoryId)));
    }

    @GetMapping("/missions")
    public ResponseEntity<ApiResponse<List<MissionResponse>>> getMyMissions(
            @RequestHeader("X-User-Id") String providerId) {

        return ResponseEntity.ok(
                ApiResponse.success(missionUseCase.getMissionsByProvider(providerId)));
    }

    @GetMapping("/missions/{id}")
    public ResponseEntity<ApiResponse<MissionResponse>> getMissionById(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String providerId,
            @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.ok(
                ApiResponse.success(missionUseCase.getMissionById(id, providerId, role)));
    }
}