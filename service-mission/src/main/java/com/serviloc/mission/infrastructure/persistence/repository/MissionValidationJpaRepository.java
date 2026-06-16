// MissionValidationJpaRepository.java
package com.serviloc.mission.infrastructure.persistence.repository;

import com.serviloc.mission.infrastructure.persistence.entity.MissionValidationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MissionValidationJpaRepository extends JpaRepository<MissionValidationJpaEntity, String> {
    Optional<MissionValidationJpaEntity> findByMissionIdAndRole(String missionId, String role);
    long countByMissionId(String missionId);
}