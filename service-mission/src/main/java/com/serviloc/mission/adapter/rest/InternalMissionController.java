// adapter/rest/InternalMissionController.java
package com.serviloc.mission.adapter.rest;

import com.serviloc.mission.application.dto.response.ApiResponse;
import com.serviloc.mission.application.dto.response.UserMissionStatsResponse;
import com.serviloc.mission.application.service.MissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal")
@Tag(name = "Internal", description = "Endpoints inter-services — non exposés au Gateway")
public class InternalMissionController {

    private final MissionService missionService;

    public InternalMissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    @Operation(
            summary = "Statistiques de missions d'un utilisateur",
            description = "Retourne le nombre total de missions et le nombre de missions TERMINEE pour un userId donné (client ou prestataire). Appelé par Service Utilisateurs."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Stats retournées",
                    content = @Content(schema = @Schema(implementation = UserMissionStatsResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Header X-Internal-Token manquant", content = @Content)
    })
    @GetMapping("/missions/stats/{userId}")
    public ResponseEntity<ApiResponse<UserMissionStatsResponse>> getMissionStats(
            @Parameter(hidden = true) @RequestHeader("X-Internal-Token") String internalToken,
            @Parameter(description = "ID de l'utilisateur (client ou prestataire)", required = true)
            @PathVariable String userId
    ) {
        UserMissionStatsResponse stats = missionService.getMissionStatsForUser(userId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}