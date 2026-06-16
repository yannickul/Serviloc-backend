// MissionStepJpaEntity.java
package com.serviloc.mission.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "mission_steps")
public class MissionStepJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private MissionJpaEntity mission;

    @Column(nullable = false)
    private String label;

    private boolean completed;

    @Column(name = "step_order")
    private int order;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public MissionJpaEntity getMission() { return mission; }
    public void setMission(MissionJpaEntity mission) { this.mission = mission; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}