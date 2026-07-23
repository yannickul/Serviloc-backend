// infrastructure/persistence/LitigeMotifJpaRepository.java
package com.serviloc.litiges.infrastructure.persistence.repository;

import com.serviloc.litiges.infrastructure.persistence.entity.LitigeMotifJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LitigeMotifJpaRepository extends JpaRepository<LitigeMotifJpaEntity, String> {}