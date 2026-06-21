// infrastructure/persistence/ResolutionJpaEntity.java
package com.serviloc.litiges.infrastructure.persistence.entity;

import com.serviloc.litiges.domain.model.ResolutionType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "resolutions")
public class ResolutionJpaEntity {

    @Id
    private String id;
    private String litigeId;
    private String agentId;

    @Enumerated(EnumType.STRING)
    private ResolutionType type;

    private BigDecimal refundAmount;
    private String note;
    private Boolean clientAccepted;
    private Boolean providerAccepted;
    private Instant createdAt;

    public ResolutionJpaEntity() {}

    public String getId()                { return id; }
    public String getLitigeId()          { return litigeId; }
    public String getAgentId()           { return agentId; }
    public ResolutionType getType()      { return type; }
    public BigDecimal getRefundAmount()  { return refundAmount; }
    public String getNote()              { return note; }
    public Boolean getClientAccepted()   { return clientAccepted; }
    public Boolean getProviderAccepted() { return providerAccepted; }
    public Instant getCreatedAt()        { return createdAt; }

    public void setId(String id)                         { this.id = id; }
    public void setLitigeId(String litigeId)             { this.litigeId = litigeId; }
    public void setAgentId(String agentId)               { this.agentId = agentId; }
    public void setType(ResolutionType type)             { this.type = type; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public void setNote(String note)                     { this.note = note; }
    public void setClientAccepted(Boolean v)             { this.clientAccepted = v; }
    public void setProviderAccepted(Boolean v)           { this.providerAccepted = v; }
    public void setCreatedAt(Instant createdAt)          { this.createdAt = createdAt; }
}