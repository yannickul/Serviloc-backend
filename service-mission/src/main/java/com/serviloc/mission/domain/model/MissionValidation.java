package com.serviloc.mission.domain.model;


import java.time.Instant;

public class MissionValidation {
    private String id;
    private String missionId;
    private String validatedBy;
    private String role;
    private Instant validatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMissionId() { return missionId; }
    public void setMissionId(String missionId) { this.missionId = missionId; }
    public String getValidatedBy() { return validatedBy; }
    public void setValidatedBy(String validatedBy) { this.validatedBy = validatedBy; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Instant getValidatedAt() { return validatedAt; }
    public void setValidatedAt(Instant validatedAt) { this.validatedAt = validatedAt; }
}