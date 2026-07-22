// infrastructure/persistence/repository/LitigeJpaRepository.java
package com.serviloc.litiges.infrastructure.persistence.repository;

import com.serviloc.litiges.domain.model.LitigeStatus;
import com.serviloc.litiges.infrastructure.persistence.entity.LitigeJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LitigeJpaRepository extends JpaRepository<LitigeJpaEntity, String> {
    boolean existsByMissionIdAndStatus(String missionId, LitigeStatus status);
    List<LitigeJpaEntity> findByMissionIdAndStatus(String missionId, LitigeStatus status);
    Page<LitigeJpaEntity> findByStatusAndAgentId(LitigeStatus status, String agentId, Pageable pageable);
    Page<LitigeJpaEntity> findByStatus(LitigeStatus status, Pageable pageable);
    Page<LitigeJpaEntity> findByAgentId(String agentId, Pageable pageable);
    Optional<LitigeJpaEntity> findByTransactionIdAndStatus(String transactionId, LitigeStatus status);

    long countByStatus(LitigeStatus status);
    long countByStatusAndUpdatedAtAfter(LitigeStatus status, Instant since);

    @Query("select coalesce(sum(l.amount), 0) from LitigeJpaEntity l where l.status in :statuses")
    BigDecimal sumAmountByStatusIn(@Param("statuses") List<LitigeStatus> statuses);
}
