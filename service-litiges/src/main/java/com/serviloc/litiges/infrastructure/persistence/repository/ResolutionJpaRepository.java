// infrastructure/persistence/ResolutionJpaRepository.java
package com.serviloc.litiges.infrastructure.persistence.repository;

import com.serviloc.litiges.infrastructure.persistence.entity.ResolutionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResolutionJpaRepository extends JpaRepository<ResolutionJpaEntity, String> {
    Optional<ResolutionJpaEntity> findByLitigeId(String litigeId);
}