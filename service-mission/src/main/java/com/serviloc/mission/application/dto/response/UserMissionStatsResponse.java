// application/dto/response/UserMissionStatsResponse.java
package com.serviloc.mission.application.dto.response;

public class UserMissionStatsResponse {

    private String userId;
    private long completedMissions;
    private long totalMissions;

    public UserMissionStatsResponse(String userId, long completedMissions, long totalMissions) {
        this.userId = userId;
        this.completedMissions = completedMissions;
        this.totalMissions = totalMissions;
    }

    public String getUserId() { return userId; }
    public long getCompletedMissions() { return completedMissions; }
    public long getTotalMissions() { return totalMissions; }
}