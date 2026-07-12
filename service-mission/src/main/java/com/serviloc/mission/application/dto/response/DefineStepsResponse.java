// application/dto/response/DefineStepsResponse.java
package com.serviloc.mission.application.dto.response;

import java.util.List;

public class DefineStepsResponse {
    private String missionId;
    private List<StepResponse> steps;

    public DefineStepsResponse(String missionId, List<StepResponse> steps) {
        this.missionId = missionId;
        this.steps = steps;
    }

    public String getMissionId() { return missionId; }
    public List<StepResponse> getSteps() { return steps; }
}