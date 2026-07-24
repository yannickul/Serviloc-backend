package com.serviloc.mission.infrastructure.persistence.adapter;

import com.serviloc.mission.domain.model.ProviderApplication;
import com.serviloc.mission.domain.repository.ProviderApllicationRepository;
import com.serviloc.mission.infrastructure.persistence.entity.ProviderApplicationJpaEntity;
import com.serviloc.mission.infrastructure.persistence.repository.ProviderApplicationJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProviderApplicationRepositoryAdapter implements ProviderApllicationRepository {

    private final ProviderApplicationJpaRepository jpaRepository;

    public ProviderApplicationRepositoryAdapter(ProviderApplicationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ProviderApplication save(ProviderApplication application) {
        ProviderApplicationJpaEntity saved = jpaRepository.save(toEntity(application));
        return toDomain(saved);
    }

    @Override
    public Optional<ProviderApplication> findByDemandIdAndProviderId(String demandId, String providerId) {
        return jpaRepository.findByDemandIdAndProviderId(demandId, providerId).map(this::toDomain);
    }

    @Override
    public List<ProviderApplication> findByDemandId(String demandId) {
        return jpaRepository.findByDemandId(demandId).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    private ProviderApplicationJpaEntity toEntity(ProviderApplication application) {
        ProviderApplicationJpaEntity entity = new ProviderApplicationJpaEntity();
        entity.setId(application.getId());
        entity.setDemandId(application.getDemandId());
        entity.setProviderId(application.getProviderId());
        entity.setAppliedAt(application.getAppliedAt());
        entity.setStatus(application.getStatus());
        return entity;
    }

    private ProviderApplication toDomain(ProviderApplicationJpaEntity entity) {
        ProviderApplication application = new ProviderApplication();
        application.setId(entity.getId());
        application.setDemandId(entity.getDemandId());
        application.setProviderId(entity.getProviderId());
        application.setAppliedAt(entity.getAppliedAt());
        application.setStatus(entity.getStatus());
        return application;
    }
}
