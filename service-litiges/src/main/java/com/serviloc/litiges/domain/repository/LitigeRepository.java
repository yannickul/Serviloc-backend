// domain/repository/LitigeRepository.java
package com.serviloc.litiges.domain.repository;

import com.serviloc.litiges.domain.model.Litige;
import com.serviloc.litiges.domain.model.LitigeStatus;

import java.util.List;
import java.util.Optional;

public interface LitigeRepository {
    Litige save(Litige litige);
    Optional<Litige> findById(String id);
    List<Litige> findAll(int page, int limit, LitigeStatus status, String agentId);
    long count(LitigeStatus status, String agentId);
    boolean existsByMissionIdAndStatus(String missionId, LitigeStatus status);
    List<Litige> findByMissionIdAndStatus(String missionId, LitigeStatus status);
}