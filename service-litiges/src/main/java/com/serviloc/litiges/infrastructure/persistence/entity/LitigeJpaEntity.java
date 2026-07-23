// infrastructure/persistence/LitigeJpaEntity.java
package com.serviloc.litiges.infrastructure.persistence.entity;

import com.serviloc.litiges.domain.model.LitigeStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "litiges")
public class LitigeJpaEntity {

    @Id
    private String id;
    private String reference;
    private String demandId;
    private String missionId;
    private String transactionId;
    private String clientId;
    private String providerId;
    private String agentId;
    private String motifId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "litige_evidence_ids", joinColumns = @JoinColumn(name = "litige_id"))
    @Column(name = "evidence_id")
    private List<String> evidenceIds;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private LitigeStatus status;

    // La résolution est stockée dans sa propre table — on référence par litigeId
    // (pas de @OneToOne ici pour garder l'indépendance de cycle de vie)

    private Instant createdAt;
    private Instant updatedAt;

    public LitigeJpaEntity() {}

    public String getId()                { return id; }
    public String getReference()         { return reference; }
    public String getDemandId()          { return demandId; }
    public String getMissionId()         { return missionId; }
    public String getTransactionId() { return transactionId; }
    public String getClientId()          { return clientId; }
    public String getProviderId()        { return providerId; }
    public String getAgentId()           { return agentId; }
    public String getMotifId()           { return motifId; }
    public String getDescription()       { return description; }
    public List<String> getEvidenceIds() { return evidenceIds; }
    public BigDecimal getAmount()        { return amount; }
    public LitigeStatus getStatus()      { return status; }
    public Instant getCreatedAt()        { return createdAt; }
    public Instant getUpdatedAt()        { return updatedAt; }

    public void setId(String id)                      { this.id = id; }
    public void setReference(String reference)        { this.reference = reference; }
    public void setDemandId(String demandId)          { this.demandId = demandId; }
    public void setMissionId(String missionId)        { this.missionId = missionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public void setClientId(String clientId)          { this.clientId = clientId; }
    public void setProviderId(String providerId)      { this.providerId = providerId; }
    public void setAgentId(String agentId)            { this.agentId = agentId; }
    public void setMotifId(String motifId)            { this.motifId = motifId; }
    public void setDescription(String description)    { this.description = description; }
    public void setEvidenceIds(List<String> ids)      { this.evidenceIds = ids; }
    public void setAmount(BigDecimal amount)          { this.amount = amount; }
    public void setStatus(LitigeStatus status)        { this.status = status; }
    public void setCreatedAt(Instant createdAt)       { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt)       { this.updatedAt = updatedAt; }
}