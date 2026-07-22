// domain/repository/DemandRepository.java
package com.serviloc.mission.domain.repository;

import com.serviloc.mission.domain.model.Demand;
import com.serviloc.mission.domain.model.DemandStatus;

import java.util.List;
import java.util.Optional;

public interface DemandRepository {
    Demand save(Demand demand);
    Optional<Demand> findById(String id);
    List<Demand> findByClientId(String clientId, DemandStatus status, int page, int limit);
    List<Demand> findByStatus(DemandStatus status);
    List<Demand> findAll(int page, int limit);
    void deleteById(String id);
    long countByClientId(String clientId, DemandStatus status);
    long countByStatus(DemandStatus status);
    long countAll();
}