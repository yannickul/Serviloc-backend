package com.serviloc.utilisateurs.domain.model;

import java.util.UUID;

/**
 * Entité domaine AgentProfile — zéro import Spring/JPA.
 * Contient les informations spécifiques au rôle agent.
 */
public class AgentProfile {

    private final UUID id;
    private final UUID userId;
    private String agentCode;      // ex: AGT-007
    private String department;     // ex: "Service Client"
    private int assignedLitigesCount;

    public static AgentProfile create(UUID userId, String agentCode, String department) {
        if (agentCode == null || agentCode.isBlank())
            throw new IllegalArgumentException("Code agent obligatoire");
        if (department == null || department.isBlank())
            throw new IllegalArgumentException("Département obligatoire");

        return new AgentProfile(UUID.randomUUID(), userId, agentCode, department, 0);
    }

    private AgentProfile(UUID id, UUID userId, String agentCode,
                         String department, int assignedLitigesCount) {
        this.id = id;
        this.userId = userId;
        this.agentCode = agentCode;
        this.department = department;
        this.assignedLitigesCount = assignedLitigesCount;
    }

    // ─── Business methods ─────────────────────────────────────────
    public void incrementAssignedLitiges() { this.assignedLitigesCount++; }
    public void decrementAssignedLitiges() {
        if (this.assignedLitigesCount > 0) this.assignedLitigesCount--;
    }

    // ─── Getters ──────────────────────────────────────────────────
    public UUID getId()                   { return id; }
    public UUID getUserId()               { return userId; }
    public String getAgentCode()          { return agentCode; }
    public String getDepartment()         { return department; }
    public int getAssignedLitigesCount()  { return assignedLitigesCount; }
}