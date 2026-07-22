// infrastructure/persistence/repository/DemandJpaRepository.java
package com.serviloc.mission.infrastructure.persistence.repository;

import com.serviloc.mission.infrastructure.persistence.entity.DemandJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DemandJpaRepository extends JpaRepository<DemandJpaEntity, String> {
    Page<DemandJpaEntity> findByClientId(String clientId, Pageable pageable);
    Page<DemandJpaEntity> findByClientIdAndStatus(String clientId, String status, Pageable pageable);
    List<DemandJpaEntity> findByStatus(String status);
    long countByClientId(String clientId);
    long countByClientIdAndStatus(String clientId, String status);
    long countByStatus(String status);
}