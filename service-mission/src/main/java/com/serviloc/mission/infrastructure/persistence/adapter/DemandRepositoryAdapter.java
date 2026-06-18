// infrastructure/persistence/adapter/DemandRepositoryAdapter.java
package com.serviloc.mission.infrastructure.persistence.adapter;

import com.serviloc.mission.application.dto.request.BudgetRangeDto;
import com.serviloc.mission.application.dto.request.LocationDto;
import com.serviloc.mission.domain.model.*;
import com.serviloc.mission.domain.repository.DemandRepository;
import com.serviloc.mission.infrastructure.persistence.entity.DemandJpaEntity;
import com.serviloc.mission.infrastructure.persistence.repository.DemandJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DemandRepositoryAdapter implements DemandRepository {

    private final DemandJpaRepository jpaRepository;

    public DemandRepositoryAdapter(DemandJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Demand save(Demand demand) {
        DemandJpaEntity entity = toEntity(demand);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Demand> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Demand> findByClientId(
            String clientId, DemandStatus status, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<DemandJpaEntity> result = status != null
                ? jpaRepository.findByClientIdAndStatus(clientId, status.name(), pageable)
                : jpaRepository.findByClientId(clientId, pageable);
        return result.getContent().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Demand> findByStatus(DemandStatus status) {
        return jpaRepository.findByStatus(status.name())
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Demand> findAll(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        return jpaRepository.findAll(pageable)
                .getContent().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long countByClientId(String clientId, DemandStatus status) {
        return status != null
                ? jpaRepository.countByClientIdAndStatus(clientId, status.name())
                : jpaRepository.countByClientId(clientId);
    }

    @Override
    public long countByStatus(DemandStatus status) {
        return jpaRepository.countByStatus(status.name());
    }

    @Override
    public long countAll() {
        return jpaRepository.count();
    }

    private DemandJpaEntity toEntity(Demand demand) {
        DemandJpaEntity entity = new DemandJpaEntity();
        entity.setId(demand.getId());
        entity.setClientId(demand.getClientId());
        entity.setCategoryId(demand.getCategoryId());
        entity.setDescription(demand.getDescription());
        entity.setPhotoIds(demand.getPhotoIds());
        entity.setStatus(demand.getStatus().name());
        entity.setIsUrgent(demand.isUrgent());
        entity.setCreatedAt(demand.getCreatedAt());
        entity.setProviderId(demand.getProviderId());
        entity.setQuoteId(demand.getQuoteId());
        if (demand.getLocation() != null) {
            entity.setLat(demand.getLocation().lat());
            entity.setLng(demand.getLocation().lng());
            entity.setAddress(demand.getLocation().address());
        }
        if (demand.getEstimatedBudget() != null) {
            entity.setBudgetMin(demand.getEstimatedBudget().min());
            entity.setBudgetMax(demand.getEstimatedBudget().max());
        }
        return entity;
    }

    private Demand toDomain(DemandJpaEntity entity) {
        Demand demand = new Demand();
        demand.setId(entity.getId());
        demand.setClientId(entity.getClientId());
        demand.setCategoryId(entity.getCategoryId());
        demand.setDescription(entity.getDescription());
        demand.setPhotoIds(entity.getPhotoIds());
        demand.setStatus(DemandStatus.valueOf(entity.getStatus()));
        demand.setIsUrgent(entity.isUrgent());
        demand.setCreatedAt(entity.getCreatedAt());
        demand.setProviderId(entity.getProviderId());
        demand.setQuoteId(entity.getQuoteId());
        if (entity.getLat() != 0 || entity.getLng() != 0) {
            demand.setLocation(new Location(
                    entity.getLat(), entity.getLng(), entity.getAddress()));
        }
        if (entity.getBudgetMin() != null) {
            demand.setEstimatedBudget(new BudgetRange(
                    entity.getBudgetMin(), entity.getBudgetMax()));
        }
        return demand;
    }
}