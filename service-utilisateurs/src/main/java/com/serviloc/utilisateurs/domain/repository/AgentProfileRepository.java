package com.serviloc.utilisateurs.domain.repository;

import com.serviloc.utilisateurs.domain.model.AgentProfile;
import java.util.Optional;
import java.util.UUID;

public interface AgentProfileRepository {
    AgentProfile save(AgentProfile profile);
    Optional<AgentProfile> findByUserId(UUID userId);
    Optional<AgentProfile> findByAgentCode(String agentCode);
    boolean existsByAgentCode(String agentCode);
    int countActiveAgents();
}