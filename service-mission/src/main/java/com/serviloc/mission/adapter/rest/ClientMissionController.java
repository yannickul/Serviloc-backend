// adapter/rest/ClientMissionController.java
package com.serviloc.mission.adapter.rest;

import com.serviloc.mission.application.dto.request.CreateLitigeRequest;
import com.serviloc.mission.application.dto.request.RateMissionRequest;
import com.serviloc.mission.application.dto.response.ApiResponse;
import com.serviloc.mission.application.dto.response.MissionResponse;
import com.serviloc.mission.application.dto.response.PagedResponse;
import com.serviloc.mission.application.dto.response.ValidateMissionResponse;
import com.serviloc.mission.application.port.in.MissionUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/client")
public class ClientMissionController {

    private final MissionUseCase missionUseCase;

    public ClientMissionController(MissionUseCase missionUseCase) {
        this.missionUseCase = missionUseCase;
    }

    @GetMapping("/missions/{id}")
    public ResponseEntity<ApiResponse<MissionResponse>> getMissionById(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String clientId,
            @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.ok(
                ApiResponse.success(missionUseCase.getMissionById(id, clientId, role)));
    }

    @GetMapping("/missions")
    public ResponseEntity<PagedResponse<MissionResponse>> getMyMissions(
            @RequestHeader("X-User-Id") String clientId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {

        return ResponseEntity.ok(missionUseCase.getMissionsByClient(clientId, status, page, limit));
    }

    @PostMapping("/missions/{id}/validate")
    public ResponseEntity<ApiResponse<ValidateMissionResponse>> validateMission(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String clientId) {

        ValidateMissionResponse response = missionUseCase.validateMission(id, clientId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/missions/{id}/rate")
    public ResponseEntity<ApiResponse<Void>> rate(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String clientId,
            @Valid @RequestBody RateMissionRequest request) {

        missionUseCase.rateAsClient(id, clientId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/missions/{id}/litige")
    public ResponseEntity<ApiResponse<Void>> declareLitige(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String clientId,
            @Valid @RequestBody CreateLitigeRequest request) {

        missionUseCase.declareLitigeAsClient(id, clientId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}