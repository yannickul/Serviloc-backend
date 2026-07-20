package com.serviloc.mission.adapter.rest;

import com.serviloc.mission.application.dto.request.CreateQuoteRequest;
import com.serviloc.mission.application.dto.request.UpdateQuoteRequest;
import com.serviloc.mission.application.dto.response.ApiResponse;
import com.serviloc.mission.application.dto.response.DemandResponse;
import com.serviloc.mission.application.dto.response.MissionResponse;
import com.serviloc.mission.application.dto.response.QuoteResponse;
import com.serviloc.mission.application.port.in.DemandUseCase;
import com.serviloc.mission.application.port.in.MissionUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/demands/{id}/quote")
    public ResponseEntity<ApiResponse<QuoteResponse>> createQuote(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String providerId,
            @Valid @RequestBody CreateQuoteRequest request) {

        QuoteResponse response = demandUseCase.createQuoteForDemand(id, providerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/demands/{id}/quote")
    public ResponseEntity<ApiResponse<QuoteResponse>> getQuote(
            @PathVariable String id) {

        return ResponseEntity.ok(ApiResponse.success(demandUseCase.getQuoteForDemand(id)));
    }

    @PutMapping("/demands/{id}/quote")
    public ResponseEntity<ApiResponse<QuoteResponse>> updateQuote(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String providerId,
            @Valid @RequestBody UpdateQuoteRequest request) {

        QuoteResponse response = demandUseCase.updateQuoteForDemand(id, providerId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/missions")
    public ResponseEntity<ApiResponse<List<MissionResponse>>> getMyMissions(
            @RequestHeader("X-User-Id") String providerId,
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(
                ApiResponse.success(missionUseCase.getMissionsByProvider(providerId, status)));
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