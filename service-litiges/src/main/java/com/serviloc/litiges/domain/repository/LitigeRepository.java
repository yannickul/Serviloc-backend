// domain/repository/LitigeRepository.java
package com.serviloc.litiges.domain.repository;

import com.serviloc.litiges.domain.model.Litige;
import com.serviloc.litiges.domain.model.LitigeStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LitigeRepository {
    Litige save(Litige litige);
    Optional<Litige> findById(String id);
    List<Litige> findAll(int page, int limit, LitigeStatus status, String agentId);
    long count(LitigeStatus status, String agentId);
    boolean existsByMissionIdAndStatus(String missionId, LitigeStatus status);
    List<Litige> findByMissionIdAndStatus(String missionId, LitigeStatus status);
    Optional<Litige> findByTransactionIdAndStatus(String transactionId, LitigeStatus status);

    // Utilisés par GET /admin/litiges (metrics) et GET /admin/litiges/stats
    long countByStatus(LitigeStatus status);
    long countByStatusAndUpdatedAtAfter(LitigeStatus status, Instant since);
    BigDecimal sumAmountByStatusIn(List<LitigeStatus> statuses);
}
