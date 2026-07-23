// domain/model/Resolution.java
package com.serviloc.litiges.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public class Resolution {

    private String id;
    private String litigeId;
    private String agentId;
    private ResolutionType type;
    private BigDecimal refundAmount;   // null si AUCUN_REMBOURSEMENT ou REJET
    private String note;
    private Boolean clientAccepted;    // null si pas encore répondu
    private Boolean providerAccepted;  // null si pas encore répondu
    private Instant createdAt;

    public Resolution() {}

    // Getters
    public String getId()                  { return id; }
    public String getLitigeId()            { return litigeId; }
    public String getAgentId()             { return agentId; }
    public ResolutionType getType()        { return type; }
    public BigDecimal getRefundAmount()    { return refundAmount; }
    public String getNote()                { return note; }
    public Boolean getClientAccepted()     { return clientAccepted; }
    public Boolean getProviderAccepted()   { return providerAccepted; }
    public Instant getCreatedAt()          { return createdAt; }

    // Setters
    public void setId(String id)                          { this.id = id; }
    public void setLitigeId(String litigeId)              { this.litigeId = litigeId; }
    public void setAgentId(String agentId)                { this.agentId = agentId; }
    public void setType(ResolutionType type)              { this.type = type; }
    public void setRefundAmount(BigDecimal refundAmount)  { this.refundAmount = refundAmount; }
    public void setNote(String note)                      { this.note = note; }
    public void setClientAccepted(Boolean clientAccepted) { this.clientAccepted = clientAccepted; }
    public void setProviderAccepted(Boolean v)            { this.providerAccepted = v; }
    public void setCreatedAt(Instant createdAt)           { this.createdAt = createdAt; }
}