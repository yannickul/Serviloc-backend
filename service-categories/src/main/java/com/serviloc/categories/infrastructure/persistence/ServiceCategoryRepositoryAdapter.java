package com.serviloc.categories.infrastructure.persistence;

import com.serviloc.categories.domain.model.CategoryId;
import com.serviloc.categories.domain.model.ServiceCategory;
import com.serviloc.categories.domain.repository.ServiceCategoryRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ServiceCategoryRepositoryAdapter implements ServiceCategoryRepository {

    private final ServiceCategoryJpaRepository jpaRepository;
    private final ServiceCategoryPersistenceMapper mapper;

    public ServiceCategoryRepositoryAdapter(ServiceCategoryJpaRepository jpaRepository,
                                             ServiceCategoryPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<ServiceCategory> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<ServiceCategory> findById(CategoryId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<ServiceCategory> findByLabelIgnoreCase(String label) {
        return jpaRepository.findByLabelIgnoreCase(label).map(mapper::toDomain);
    }

    @Override
    public boolean existsByLabelIgnoreCase(String label) {
        return jpaRepository.existsByLabelIgnoreCase(label);
    }

    @Override
    public ServiceCategory save(ServiceCategory category) {
        ServiceCategoryJpaEntity saved = jpaRepository.save(mapper.toJpaEntity(category));
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(CategoryId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public boolean existsById(CategoryId id) {
        return jpaRepository.existsById(id.value());
    }

    @Override
    public long totalDemandCount() {
        return jpaRepository.sumDemandCount();
    }
}
