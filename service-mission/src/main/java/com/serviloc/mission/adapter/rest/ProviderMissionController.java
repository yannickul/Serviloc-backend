// adapter/rest/ProviderMissionController.java
package com.serviloc.mission.adapter.rest;

import com.serviloc.mission.application.dto.request.CreateLitigeRequest;
import com.serviloc.mission.application.dto.request.CreateStepsRequest;
import com.serviloc.mission.application.dto.request.RateMissionRequest;
import com.serviloc.mission.application.dto.response.*;
import com.serviloc.mission.application.port.in.MissionUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/provider")
public class ProviderMissionController {

    private final MissionUseCase missionUseCase;

    public ProviderMissionController(MissionUseCase missionUseCase) {
        this.missionUseCase = missionUseCase;
    }

    @PostMapping("/missions/{id}/start")
    public ResponseEntity<ApiResponse<StartMissionResponse>> startMission(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String providerId) {

        StartMissionResponse response = missionUseCase.startMission(id, providerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/missions/{id}/complete")
    public ResponseEntity<ApiResponse<CompleteMissionResponse>> completeMission(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String providerId) {

        CompleteMissionResponse response = missionUseCase.completeMission(id, providerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/missions/{id}/steps/{stepId}")
    public ResponseEntity<ApiResponse<Void>> updateStep(
            @PathVariable String id,
            @PathVariable String stepId,
            @RequestHeader("X-User-Id") String providerId) {

        missionUseCase.updateStep(id, stepId, providerId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/missions/{id}/rate")
    public ResponseEntity<ApiResponse<Void>> rate(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String providerId,
            @Valid @RequestBody RateMissionRequest request) {

        missionUseCase.rateAsProvider(id, providerId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/missions/{id}/litige")
    public ResponseEntity<ApiResponse<Void>> declareLitige(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String providerId,
            @Valid @RequestBody CreateLitigeRequest request) {

        missionUseCase.declareLitigeAsProvider(id, providerId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/missions/{id}/steps")
    public ResponseEntity<ApiResponse<DefineStepsResponse>> defineSteps(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String providerId,
            @Valid @RequestBody CreateStepsRequest request) {

        DefineStepsResponse response = missionUseCase.defineSteps(id, providerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}