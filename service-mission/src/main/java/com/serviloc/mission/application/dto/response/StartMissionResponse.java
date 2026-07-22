// application/dto/response/StartMissionResponse.java
package com.serviloc.mission.application.dto.response;

import java.time.Instant;

public class StartMissionResponse {
    private String missionId;
    private String status;
    private Instant startedAt;

    public StartMissionResponse(String missionId, String status, Instant startedAt) {
        this.missionId = missionId;
        this.status = status;
        this.startedAt = startedAt;
    }

    public String getMissionId() { return missionId; }
    public String getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
}