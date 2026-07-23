// application/dto/request/CreateLitigeRequest.java
package com.serviloc.mission.application.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class CreateLitigeRequest {

    @NotBlank(message = "La raison du litige est obligatoire")
    private String reason;

    private String description;

    private List<String> evidencePhotoIds;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getEvidencePhotoIds() { return evidencePhotoIds; }
    public void setEvidencePhotoIds(List<String> evidencePhotoIds) { this.evidencePhotoIds = evidencePhotoIds; }
}
