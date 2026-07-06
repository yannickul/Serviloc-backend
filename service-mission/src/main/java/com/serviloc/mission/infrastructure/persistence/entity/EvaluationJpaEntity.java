// EvaluationJpaEntity.java
package com.serviloc.mission.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "evaluations")
public class EvaluationJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "mission_id", nullable = false, length = 36)
    private String missionId;

    @Column(name = "evaluator_id", nullable = false, length = 36)
    private String evaluatorId;

    @Column(name = "target_id", nullable = false, length = 36)
    private String targetId;

    @Column(name = "target_role", nullable = false, length = 20)
    private String targetRole;

    @Column(nullable = false)
    private int rating;

    @ElementCollection
    @CollectionTable(name = "evaluation_criteria", joinColumns = @JoinColumn(name = "evaluation_id"))
    @MapKeyColumn(name = "criteria_key")
    @Column(name = "criteria_value")
    private Map<String, Integer> criteria;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMissionId() { return missionId; }
    public void setMissionId(String missionId) { this.missionId = missionId; }
    public String getEvaluatorId() { return evaluatorId; }
    public void setEvaluatorId(String evaluatorId) { this.evaluatorId = evaluatorId; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public Map<String, Integer> getCriteria() { return criteria; }
    public void setCriteria(Map<String, Integer> criteria) { this.criteria = criteria; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}