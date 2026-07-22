// ProviderApplicationJpaEntity.java
package com.serviloc.mission.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "provider_applications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"demand_id", "provider_id"})
)
public class ProviderApplicationJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "demand_id", nullable = false, length = 36)
    private String demandId;

    @Column(name = "provider_id", nullable = false, length = 36)
    private String providerId;

    @Column(name = "applied_at", nullable = false, updatable = false)
    private Instant appliedAt;

    @Column(nullable = false, length = 20)
    private String status; // PENDING | SELECTED | REJECTED

    @PrePersist
    public void prePersist() {
        if (appliedAt == null) appliedAt = Instant.now();
    }

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