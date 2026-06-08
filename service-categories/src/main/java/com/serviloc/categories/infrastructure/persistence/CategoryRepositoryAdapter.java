package com.serviloc.categories.infrastructure.persistence;

import com.serviloc.categories.domain.model.ServiceCategory;
import com.serviloc.categories.domain.repository.CategoryRepository;
import com.serviloc.categories.domain.exception.CategoryNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository repo;

    public CategoryRepositoryAdapter(CategoryJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<ServiceCategory> findAll() {
        return repo.findAll().stream()
                   .map(this::toDomain)
                   .toList();
    }

    @Override
    public ServiceCategory findById(Long id) {
        return repo.findById(id)
                   .map(this::toDomain)
                   .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Override
    public ServiceCategory save(ServiceCategory c) {
        CategoryJpaEntity entity = toEntity(c);
        CategoryJpaEntity saved = repo.save(entity);
        return toDomain(saved);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    private ServiceCategory toDomain(CategoryJpaEntity e) {
        return new ServiceCategory(
                e.getId(), e.getLabel(), e.getIconKey(), e.getColor(),
                e.getDemandCount(), e.getPercentageShare()
        );
    }

    private CategoryJpaEntity toEntity(ServiceCategory c) {
        CategoryJpaEntity e = new CategoryJpaEntity();
        e.setId(c.id());
        e.setLabel(c.label());
        e.setIconKey(c.iconKey());
        e.setColor(c.color());
        e.setDemandCount(c.demandCount());
        e.setPercentageShare(c.percentageShare());
        return e;
    }
}

