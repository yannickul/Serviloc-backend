package com.serviloc.mission.infrastructure.persistence.repository;

import com.serviloc.mission.infrastructure.persistence.entity.MissionValidationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionValidationJpaRepository extends JpaRepository<MissionValidationJpaEntity, String> {
    boolean existsByMissionIdAndRole(String missionId, String role);
    long countByMissionId(String missionId);
}