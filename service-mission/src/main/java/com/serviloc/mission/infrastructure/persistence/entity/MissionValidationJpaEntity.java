// MissionValidationJpaEntity.java
package com.serviloc.mission.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "mission_validations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"mission_id", "role"})
)
public class MissionValidationJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "mission_id", nullable = false, length = 36)
    private String missionId;

    @Column(name = "validated_by", nullable = false, length = 36)
    private String validatedBy;

    @Column(nullable = false, length = 20)
    private String role; // CLIENT | PROVIDER

    @Column(name = "validated_at", nullable = false)
    private Instant validatedAt;

    @PrePersist
    public void prePersist() {
        if (validatedAt == null) validatedAt = Instant.now();
    }

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