// infrastructure/persistence/repository/LitigeMessageJpaRepository.java
package com.serviloc.litiges.infrastructure.persistence.repository;

import com.serviloc.litiges.infrastructure.persistence.entity.LitigeMessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LitigeMessageJpaRepository extends JpaRepository<LitigeMessageJpaEntity, String> {
    List<LitigeMessageJpaEntity> findByLitigeIdOrderBySentAtAsc(String litigeId);
}
