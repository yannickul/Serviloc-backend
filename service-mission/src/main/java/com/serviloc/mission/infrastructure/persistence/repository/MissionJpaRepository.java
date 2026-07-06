// MissionJpaRepository.java
package com.serviloc.mission.infrastructure.persistence.repository;

import com.serviloc.mission.infrastructure.persistence.entity.MissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionJpaRepository extends JpaRepository<MissionJpaEntity, String> {
    List<MissionJpaEntity> findByClientId(String clientId);
    List<MissionJpaEntity> findByProviderId(String providerId);
    List<MissionJpaEntity> findByStatus(String status);
    long countByStatus(String name);
}