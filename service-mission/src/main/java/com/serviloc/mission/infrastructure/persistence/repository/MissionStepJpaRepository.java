// infrastructure/persistence/repository/MissionStepJpaRepository.java
package com.serviloc.mission.infrastructure.persistence.repository;

import com.serviloc.mission.infrastructure.persistence.entity.MissionStepJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MissionStepJpaRepository extends JpaRepository<MissionStepJpaEntity, String> {
    List<MissionStepJpaEntity> findByMissionId(String missionId);
}