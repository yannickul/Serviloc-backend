// infrastructure/persistence/LitigeRepositoryAdapter.java
package com.serviloc.litiges.infrastructure.persistence.adapter;

import com.serviloc.litiges.domain.model.Litige;
import com.serviloc.litiges.domain.model.LitigeStatus;
import com.serviloc.litiges.domain.model.Resolution;
import com.serviloc.litiges.domain.repository.LitigeRepository;
import com.serviloc.litiges.infrastructure.persistence.entity.LitigeJpaEntity;
import com.serviloc.litiges.infrastructure.persistence.entity.ResolutionJpaEntity;
import com.serviloc.litiges.infrastructure.persistence.repository.LitigeJpaRepository;
import com.serviloc.litiges.infrastructure.persistence.repository.ResolutionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LitigeRepositoryAdapter implements LitigeRepository {

    private final LitigeJpaRepository litigeJpaRepository;
    private final ResolutionJpaRepository resolutionJpaRepository;

    @Override
    public Litige save(Litige litige) {
        LitigeJpaEntity entity = toEntity(litige);
        LitigeJpaEntity saved = litigeJpaRepository.save(entity);
        return toDomain(saved, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Litige> findById(String id) {
        return litigeJpaRepository.findById(id)
                .map(entity -> {
                    ResolutionJpaEntity res = resolutionJpaRepository
                            .findByLitigeId(entity.getId()).orElse(null);
                    return toDomain(entity, res);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Litige> findAll(int page, int limit, LitigeStatus status, String agentId) {
        PageRequest pageable = PageRequest.of(page, limit);
        Page<LitigeJpaEntity> result;

        if (status != null && agentId != null) {
            result = litigeJpaRepository.findByStatusAndAgentId(status, agentId, pageable);
        } else if (status != null) {
            result = litigeJpaRepository.findByStatus(status, pageable);
        } else if (agentId != null) {
            result = litigeJpaRepository.findByAgentId(agentId, pageable);
        } else {
            result = litigeJpaRepository.findAll(pageable);
        }

        return result.getContent().stream()
                .map(entity -> {
                    ResolutionJpaEntity res = resolutionJpaRepository
                            .findByLitigeId(entity.getId()).orElse(null);
                    return toDomain(entity, res);
                })
                .toList();
    }

    @Override
    public long count(LitigeStatus status, String agentId) {
        if (status != null && agentId != null) {
            return litigeJpaRepository.findByStatusAndAgentId(status, agentId, PageRequest.of(0, 1)).getTotalElements();
        } else if (status != null) {
            return litigeJpaRepository.findByStatus(status, PageRequest.of(0, 1)).getTotalElements();
        } else if (agentId != null) {
            return litigeJpaRepository.findByAgentId(agentId, PageRequest.of(0, 1)).getTotalElements();
        }
        return litigeJpaRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByMissionIdAndStatus(String missionId, LitigeStatus status) {
        return litigeJpaRepository.existsByMissionIdAndStatus(missionId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Litige> findByMissionIdAndStatus(String missionId, LitigeStatus status) {
        return litigeJpaRepository.findByMissionIdAndStatus(missionId, status).stream()
                .map(entity -> toDomain(entity, null))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Litige> findByTransactionIdAndStatus(String transactionId, LitigeStatus status) {
        return litigeJpaRepository.findByTransactionIdAndStatus(transactionId, status)
                .map(entity -> {
                    ResolutionJpaEntity res = resolutionJpaRepository
                            .findByLitigeId(entity.getId()).orElse(null);
                    return toDomain(entity, res);
                });
    }
    // ── Mapping domain → JPA ──────────────────────────────────────────────────

    private LitigeJpaEntity toEntity(Litige litige) {
        LitigeJpaEntity e = new LitigeJpaEntity();
        e.setId(litige.getId());
        e.setReference(litige.getReference());
        e.setDemandId(litige.getDemandId());
        e.setMissionId(litige.getMissionId());
        e.setTransactionId(litige.getTransactionId());
        e.setClientId(litige.getClientId());
        e.setProviderId(litige.getProviderId());
        e.setAgentId(litige.getAgentId());
        e.setMotifId(litige.getMotifId());
        e.setDescription(litige.getDescription());
        e.setEvidenceIds(litige.getEvidenceIds());
        e.setAmount(litige.getAmount());
        e.setStatus(litige.getStatus());
        e.setCreatedAt(litige.getCreatedAt());
        e.setUpdatedAt(litige.getUpdatedAt());
        return e;
    }

    // ── Mapping JPA → domain ──────────────────────────────────────────────────

    private Litige toDomain(LitigeJpaEntity e, ResolutionJpaEntity res) {
        Litige litige = new Litige();
        litige.setId(e.getId());
        litige.setReference(e.getReference());
        litige.setDemandId(e.getDemandId());
        litige.setMissionId(e.getMissionId());
        litige.setTransactionId(e.getTransactionId());
        litige.setClientId(e.getClientId());
        litige.setProviderId(e.getProviderId());
        litige.setAgentId(e.getAgentId());
        litige.setMotifId(e.getMotifId());
        litige.setDescription(e.getDescription());
        litige.setEvidenceIds(e.getEvidenceIds() != null
                ? new java.util.ArrayList<>(e.getEvidenceIds())
                : new java.util.ArrayList<>());
        litige.setAmount(e.getAmount());
        litige.setStatus(e.getStatus());
        litige.setCreatedAt(e.getCreatedAt());
        litige.setUpdatedAt(e.getUpdatedAt());
        litige.setResolution(res != null ? resolutionToDomain(res) : null);
        return litige;
    }

    private Resolution resolutionToDomain(ResolutionJpaEntity r) {
        Resolution res = new Resolution();
        res.setId(r.getId());
        res.setLitigeId(r.getLitigeId());
        res.setAgentId(r.getAgentId());
        res.setType(r.getType());
        res.setRefundAmount(r.getRefundAmount());
        res.setNote(r.getNote());
        res.setClientAccepted(r.getClientAccepted());
        res.setProviderAccepted(r.getProviderAccepted());
        res.setCreatedAt(r.getCreatedAt());
        return res;
    }
}