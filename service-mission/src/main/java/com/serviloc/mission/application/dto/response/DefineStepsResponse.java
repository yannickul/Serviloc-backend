// application/dto/response/DefineStepsResponse.java
package com.serviloc.mission.application.dto.response;

import java.util.List;

public class DefineStepsResponse {
    private String missionId;
    private int estimatedDurationHours;
    private List<StepResponse> steps;

    public DefineStepsResponse(String missionId, int estimatedDurationHours, List<StepResponse> steps) {
        this.missionId = missionId;
        this.estimatedDurationHours = estimatedDurationHours;
        this.steps = steps;
    }

    public String getMissionId() { return missionId; }

    public List<StepResponse> getSteps() { return steps; }

    public int getEstimatedDurationHours() {
        return estimatedDurationHours;
    }
}