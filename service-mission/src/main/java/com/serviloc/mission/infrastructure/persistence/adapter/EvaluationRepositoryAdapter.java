// infrastructure/persistence/adapter/EvaluationRepositoryAdapter.java
package com.serviloc.mission.infrastructure.persistence.adapter;

import com.serviloc.mission.domain.model.Evaluation;
import com.serviloc.mission.domain.repository.EvaluationRepository;
import com.serviloc.mission.infrastructure.persistence.entity.EvaluationJpaEntity;
import com.serviloc.mission.infrastructure.persistence.repository.EvaluationJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class EvaluationRepositoryAdapter implements EvaluationRepository {

    private final EvaluationJpaRepository jpaRepository;

    public EvaluationRepositoryAdapter(EvaluationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Evaluation save(Evaluation evaluation) {
        EvaluationJpaEntity saved = jpaRepository.save(toEntity(evaluation));
        return toDomain(saved);
    }

    @Override
    public Optional<Evaluation> findByMissionIdAndEvaluatorId(String missionId, String evaluatorId) {
        return jpaRepository.findByMissionIdAndEvaluatorId(missionId, evaluatorId)
                .map(this::toDomain);
    }

    @Override
    public List<Evaluation> findByTargetId(String targetId) {
        return jpaRepository.findByTargetId(targetId)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    private EvaluationJpaEntity toEntity(Evaluation evaluation) {
        EvaluationJpaEntity entity = new EvaluationJpaEntity();
        entity.setId(evaluation.getId());
        entity.setMissionId(evaluation.getMissionId());
        entity.setEvaluatorId(evaluation.getEvaluatorId());
        entity.setTargetId(evaluation.getTargetId());
        entity.setTargetRole(evaluation.getTargetRole());
        entity.setRating(evaluation.getRating());
        entity.setComment(evaluation.getComment());
        entity.setCreatedAt(evaluation.getCreatedAt());
        // criteria : non géré dans RateMissionRequest pour l'instant (champ optionnel)
        // à brancher en Sprint 3 si le frontend envoie des critères détaillés
        entity.setCriteria(null);
        return entity;
    }

    private Evaluation toDomain(EvaluationJpaEntity entity) {
        Evaluation evaluation = new Evaluation();
        evaluation.setId(entity.getId());
        evaluation.setMissionId(entity.getMissionId());
        evaluation.setEvaluatorId(entity.getEvaluatorId());
        evaluation.setTargetId(entity.getTargetId());
        evaluation.setTargetRole(entity.getTargetRole());
        evaluation.setRating(entity.getRating());
        evaluation.setComment(entity.getComment());
        evaluation.setCreatedAt(entity.getCreatedAt());
        return evaluation;
    }
}