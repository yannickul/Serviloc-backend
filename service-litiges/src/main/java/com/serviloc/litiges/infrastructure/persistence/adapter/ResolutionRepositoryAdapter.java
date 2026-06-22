// infrastructure/persistence/ResolutionRepositoryAdapter.java
package com.serviloc.litiges.infrastructure.persistence.adapter;

import com.serviloc.litiges.domain.model.Resolution;
import com.serviloc.litiges.domain.model.ResolutionType;
import com.serviloc.litiges.domain.repository.ResolutionRepository;
import com.serviloc.litiges.infrastructure.persistence.entity.ResolutionJpaEntity;
import com.serviloc.litiges.infrastructure.persistence.repository.ResolutionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ResolutionRepositoryAdapter implements ResolutionRepository {

    private final ResolutionJpaRepository resolutionJpaRepository;

    @Override
    public Resolution save(Resolution resolution) {
        ResolutionJpaEntity entity = toEntity(resolution);
        ResolutionJpaEntity saved = resolutionJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Resolution> findByLitigeId(String litigeId) {
        return resolutionJpaRepository.findByLitigeId(litigeId).map(this::toDomain);
    }

    private ResolutionJpaEntity toEntity(Resolution r) {
        ResolutionJpaEntity e = new ResolutionJpaEntity();
        e.setId(r.getId());
        e.setLitigeId(r.getLitigeId());
        e.setAgentId(r.getAgentId());
        e.setType(r.getType());
        e.setRefundAmount(r.getRefundAmount());
        e.setNote(r.getNote());
        e.setClientAccepted(r.getClientAccepted());
        e.setProviderAccepted(r.getProviderAccepted());
        e.setCreatedAt(r.getCreatedAt());
        return e;
    }

    private Resolution toDomain(ResolutionJpaEntity e) {
        Resolution r = new Resolution();
        r.setId(e.getId());
        r.setLitigeId(e.getLitigeId());
        r.setAgentId(e.getAgentId());
        r.setType(e.getType());
        r.setRefundAmount(e.getRefundAmount());
        r.setNote(e.getNote());
        r.setClientAccepted(e.getClientAccepted());
        r.setProviderAccepted(e.getProviderAccepted());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }
}