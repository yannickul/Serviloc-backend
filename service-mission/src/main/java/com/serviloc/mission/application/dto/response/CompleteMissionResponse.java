// application/dto/response/CompleteMissionResponse.java
package com.serviloc.mission.application.dto.response;

public class CompleteMissionResponse {
    private String missionId;
    private String validatedBy;
    private boolean bothValidated;
    private String status;
    private String message;

    public CompleteMissionResponse(String missionId, String validatedBy, boolean bothValidated,
                                   String status, String message) {
        this.missionId = missionId;
        this.validatedBy = validatedBy;
        this.bothValidated = bothValidated;
        this.status = status;
        this.message = message;
    }

    public String getMissionId() { return missionId; }
    public String getValidatedBy() { return validatedBy; }
    public boolean isBothValidated() { return bothValidated; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
}