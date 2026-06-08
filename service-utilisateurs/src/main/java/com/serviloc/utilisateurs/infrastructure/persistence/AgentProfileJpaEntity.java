package com.serviloc.utilisateurs.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "agent_profiles",
        indexes = {
                @Index(name = "idx_agent_user_id",   columnList = "user_id", unique = true),
                @Index(name = "idx_agent_code",       columnList = "agent_code", unique = true)
        })
public class AgentProfileJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "agent_code", nullable = false, unique = true, length = 20)
    private String agentCode;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(name = "assigned_litiges_count", nullable = false)
    private int assignedLitigesCount = 0;

    protected AgentProfileJpaEntity() {}

    public AgentProfileJpaEntity(UUID id, UUID userId,
                                 String agentCode, String department) {
        this.id = id;
        this.userId = userId;
        this.agentCode = agentCode;
        this.department = department;
        this.assignedLitigesCount = 0;
    }

    // Getters & Setters
    public UUID getId()                          { return id; }
    public UUID getUserId()                      { return userId; }
    public String getAgentCode()                 { return agentCode; }
    public String getDepartment()                { return department; }
    public int getAssignedLitigesCount()         { return assignedLitigesCount; }
    public void setAssignedLitigesCount(int c)   { this.assignedLitigesCount = c; }
}