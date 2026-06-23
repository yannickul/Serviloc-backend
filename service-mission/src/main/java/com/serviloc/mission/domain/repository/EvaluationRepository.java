package com.serviloc.mission.domain.repository;

import com.serviloc.mission.domain.model.Evaluation;

import java.util.List;
import java.util.Optional;

public interface EvaluationRepository {
    Evaluation save(Evaluation evaluation);
    Optional<Evaluation> findByMissionIdAndEvaluatorId(String missionId, String evaluatorId);
    List<Evaluation> findByTargetId(String targetId);
}