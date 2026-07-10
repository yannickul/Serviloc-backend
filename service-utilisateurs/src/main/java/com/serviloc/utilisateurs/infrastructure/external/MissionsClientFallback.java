package com.serviloc.utilisateurs.infrastructure.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MissionsClientFallback implements MissionsClient {

    private static final Logger log = LoggerFactory.getLogger(MissionsClientFallback.class);

    @Override
    public MissionStatsResponse getMissionStats(String userId, String internalToken) {
        log.warn("[FEIGN FALLBACK] Service Missions indisponible pour userId={}", userId);
        return new MissionStatsResponse(userId, 0, 0);
    }
}