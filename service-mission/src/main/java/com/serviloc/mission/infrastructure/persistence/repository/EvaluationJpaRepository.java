// EvaluationJpaRepository.java
package com.serviloc.mission.infrastructure.persistence.repository;

import com.serviloc.mission.infrastructure.persistence.entity.EvaluationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvaluationJpaRepository extends JpaRepository<EvaluationJpaEntity, String> {
    Optional<EvaluationJpaEntity> findByMissionIdAndEvaluatorId(String missionId, String evaluatorId);
    List<EvaluationJpaEntity> findByTargetId(String targetId);
}