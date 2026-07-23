package com.serviloc.utilisateurs.infrastructure.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "service-missions", fallback = MissionsClientFallback.class)
public interface MissionsClient {

    @GetMapping("/internal/missions/stats/{userId}")
    MissionStatsResponse getMissionStats(
            @PathVariable("userId") String userId,
            @RequestHeader("X-Internal-Token") String internalToken
    );

    record MissionStatsResponse(
            String userId,
            int completedMissions,
            int totalMissions
    ) {}
}