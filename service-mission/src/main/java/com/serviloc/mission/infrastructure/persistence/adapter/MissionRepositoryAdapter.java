// infrastructure/persistence/adapter/MissionRepositoryAdapter.java
package com.serviloc.mission.infrastructure.persistence.adapter;

import com.serviloc.mission.domain.model.Location;
import com.serviloc.mission.domain.model.Mission;
import com.serviloc.mission.domain.model.MissionStatus;
import com.serviloc.mission.domain.repository.MissionRepository;
import com.serviloc.mission.infrastructure.persistence.entity.MissionJpaEntity;
import com.serviloc.mission.infrastructure.persistence.repository.MissionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MissionRepositoryAdapter implements MissionRepository {

    private final MissionJpaRepository jpaRepository;

    public MissionRepositoryAdapter(MissionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Mission save(Mission mission) {
        return toDomain(jpaRepository.save(toEntity(mission)));
    }

    @Override
    public Optional<Mission> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Mission> findByClientId(String clientId) {
        return jpaRepository.findByClientId(clientId)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Mission> findByProviderId(String providerId) {
        return jpaRepository.findByProviderId(providerId)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Mission> findByStatus(MissionStatus status) {
        return jpaRepository.findByStatus(status.name())
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countAll() {
        return jpaRepository.count();
    }

    @Override
    public long countByStatus(MissionStatus status) {
        return jpaRepository.countByStatus(status.name());
    }

    private MissionJpaEntity toEntity(Mission mission) {
        MissionJpaEntity entity = new MissionJpaEntity();
        entity.setId(mission.getId());
        entity.setDemandId(mission.getDemandId());
        entity.setQuoteId(mission.getQuoteId());
        entity.setClientId(mission.getClientId());
        entity.setProviderId(mission.getProviderId());
        entity.setCategory(mission.getCategory());
        entity.setStatus(mission.getStatus().name());
        entity.setTotalAmount(mission.getTotalAmount());
        entity.setSequesteredAmount(mission.getSequesteredAmount());
        entity.setPaymentStatus(mission.getPaymentStatus());
        entity.setStartedAt(mission.getStartedAt());
        entity.setEstimatedDurationHours(mission.getEstimatedDurationHours());
        entity.setCompletedAt(mission.getCompletedAt());
        if (mission.getLocation() != null) {
            entity.setLocationLat(mission.getLocation().lat());
            entity.setLocationLng(mission.getLocation().lng());
            entity.setLocationAddress(mission.getLocation().address());
        }
        return entity;
    }

    private Mission toDomain(MissionJpaEntity entity) {
        Mission mission = new Mission();
        mission.setId(entity.getId());
        mission.setDemandId(entity.getDemandId());
        mission.setQuoteId(entity.getQuoteId());
        mission.setClientId(entity.getClientId());
        mission.setProviderId(entity.getProviderId());
        mission.setCategory(entity.getCategory());
        mission.setStatus(MissionStatus.valueOf(entity.getStatus()));
        mission.setTotalAmount(entity.getTotalAmount());
        mission.setSequesteredAmount(entity.getSequesteredAmount());
        mission.setPaymentStatus(entity.getPaymentStatus());
        mission.setStartedAt(entity.getStartedAt());
        mission.setEstimatedDurationHours(entity.getEstimatedDurationHours());
        mission.setCompletedAt(entity.getCompletedAt());
        if (entity.getLocationLat() != 0 || entity.getLocationLng() != 0) {
            mission.setLocation(new Location(
                    entity.getLocationLat(),
                    entity.getLocationLng(),
                    entity.getLocationAddress()));
        }
        return mission;
    }
}