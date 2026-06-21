// infrastructure/persistence/LitigeJpaRepository.java
package com.serviloc.litiges.infrastructure.persistence.repository;

import com.serviloc.litiges.domain.model.LitigeStatus;
import com.serviloc.litiges.infrastructure.persistence.entity.LitigeJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LitigeJpaRepository extends JpaRepository<LitigeJpaEntity, String> {
    boolean existsByMissionIdAndStatus(String missionId, LitigeStatus status);
    List<LitigeJpaEntity> findByMissionIdAndStatus(String missionId, LitigeStatus status);
    Page<LitigeJpaEntity> findByStatusAndAgentId(LitigeStatus status, String agentId, Pageable pageable);
    Page<LitigeJpaEntity> findByStatus(LitigeStatus status, Pageable pageable);
    Page<LitigeJpaEntity> findByAgentId(String agentId, Pageable pageable);
}