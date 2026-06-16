package com.serviloc.mission.domain.model;

import java.time.Instant;

public class ProviderApplication {
    private String id;
    private String demandId;
    private String providerId;
    private Instant appliedAt;
    private String status; // PENDING | SELECTED | REJECTED

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDemandId() { return demandId; }
    public void setDemandId(String demandId) { this.demandId = demandId; }
    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public Instant getAppliedAt() { return appliedAt; }
    public void setAppliedAt(Instant appliedAt) { this.appliedAt = appliedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}