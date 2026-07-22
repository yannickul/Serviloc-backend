// ProviderApplicationJpaRepository.java
package com.serviloc.mission.infrastructure.persistence.repository;

import com.serviloc.mission.infrastructure.persistence.entity.ProviderApplicationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProviderApplicationJpaRepository extends JpaRepository<ProviderApplicationJpaEntity, String> {
    Optional<ProviderApplicationJpaEntity> findByDemandIdAndProviderId(String demandId, String providerId);
    List<ProviderApplicationJpaEntity> findByDemandId(String demandId);
}