// domain/repository/MissionRepository.java
package com.serviloc.mission.domain.repository;

import com.serviloc.mission.domain.model.Mission;
import com.serviloc.mission.domain.model.MissionStatus;

import java.util.List;
import java.util.Optional;

public interface MissionRepository {
    Mission save(Mission mission);
    Optional<Mission> findById(String id);
    List<Mission> findByClientId(String clientId);
    List<Mission> findByProviderId(String providerId);
    List<Mission> findByStatus(MissionStatus status);
    long countAll();
    long countByStatus(MissionStatus status);
}