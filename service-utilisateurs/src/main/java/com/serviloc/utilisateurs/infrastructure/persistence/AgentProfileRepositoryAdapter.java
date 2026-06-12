package com.serviloc.utilisateurs.infrastructure.persistence;

import com.serviloc.utilisateurs.domain.model.AgentProfile;
import com.serviloc.utilisateurs.domain.repository.AgentProfileRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AgentProfileRepositoryAdapter implements AgentProfileRepository {

    private final AgentProfileJpaRepository jpa;

    public AgentProfileRepositoryAdapter(AgentProfileJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public AgentProfile save(AgentProfile profile) {
        AgentProfileJpaEntity entity = jpa.findByUserId(profile.getUserId())
                .orElse(new AgentProfileJpaEntity(
                        profile.getId(), profile.getUserId(),
                        profile.getAgentCode(), profile.getDepartment()
                ));
        entity.setAssignedLitigesCount(profile.getAssignedLitigesCount());
        return toDomain(jpa.save(entity));
    }

    @Override
    public Optional<AgentProfile> findByUserId(UUID userId) {
        return jpa.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public Optional<AgentProfile> findByAgentCode(String agentCode) {
        return jpa.findByAgentCode(agentCode).map(this::toDomain);
    }

    @Override
    public boolean existsByAgentCode(String agentCode) {
        return jpa.existsByAgentCode(agentCode);
    }

    @Override
    public int countActiveAgents() {
        return (int) jpa.count();
    }

    private AgentProfile toDomain(AgentProfileJpaEntity e) {
        try {
            var ctor = AgentProfile.class.getDeclaredConstructor(
                    UUID.class, UUID.class, String.class, String.class, int.class
            );
            ctor.setAccessible(true);
            return ctor.newInstance(
                    e.getId(), e.getUserId(),
                    e.getAgentCode(), e.getDepartment(),
                    e.getAssignedLitigesCount()
            );
        } catch (Exception ex) {
            throw new RuntimeException("Erreur reconstitution AgentProfile", ex);
        }
    }
}