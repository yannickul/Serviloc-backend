package com.serviloc.utilisateurs.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentProfileJpaRepository extends JpaRepository<AgentProfileJpaEntity, UUID> {

    Optional<AgentProfileJpaEntity> findByUserId(UUID userId);

    Optional<AgentProfileJpaEntity> findByAgentCode(String agentCode);

    boolean existsByAgentCode(String agentCode);
}