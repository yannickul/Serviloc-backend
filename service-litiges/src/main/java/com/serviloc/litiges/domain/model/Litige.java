// domain/model/Litige.java
package com.serviloc.litiges.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class Litige {

    private String id;
    private String reference;      // LIT-2026-0042
    private String demandId;
    private String missionId;
    private String clientId;
    private String providerId;
    private String agentId;        // null jusqu'à assignation
    private String motifId;
    private String description;
    private List<String> evidenceIds;
    private BigDecimal amount;
    private LitigeStatus status;
    private Resolution resolution; // null jusqu'à résolution
    private Instant createdAt;
    private Instant updatedAt;

    public Litige() {}

    // Getters
    public String getId()                  { return id; }
    public String getReference()           { return reference; }
    public String getDemandId()            { return demandId; }
    public String getMissionId()           { return missionId; }
    public String getClientId()            { return clientId; }
    public String getProviderId()          { return providerId; }
    public String getAgentId()             { return agentId; }
    public String getMotifId()             { return motifId; }
    public String getDescription()         { return description; }
    public List<String> getEvidenceIds()   { return evidenceIds; }
    public BigDecimal getAmount()          { return amount; }
    public LitigeStatus getStatus()        { return status; }
    public Resolution getResolution()      { return resolution; }
    public Instant getCreatedAt()          { return createdAt; }
    public Instant getUpdatedAt()          { return updatedAt; }

    // Setters
    public void setId(String id)                       { this.id = id; }
    public void setReference(String reference)         { this.reference = reference; }
    public void setDemandId(String demandId)           { this.demandId = demandId; }
    public void setMissionId(String missionId)         { this.missionId = missionId; }
    public void setClientId(String clientId)           { this.clientId = clientId; }
    public void setProviderId(String providerId)       { this.providerId = providerId; }
    public void setAgentId(String agentId)             { this.agentId = agentId; }
    public void setMotifId(String motifId)             { this.motifId = motifId; }
    public void setDescription(String description)     { this.description = description; }
    public void setEvidenceIds(List<String> ids)       { this.evidenceIds = ids; }
    public void setAmount(BigDecimal amount)           { this.amount = amount; }
    public void setStatus(LitigeStatus status)         { this.status = status; }
    public void setResolution(Resolution resolution)   { this.resolution = resolution; }
    public void setCreatedAt(Instant createdAt)        { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt)        { this.updatedAt = updatedAt; }
}