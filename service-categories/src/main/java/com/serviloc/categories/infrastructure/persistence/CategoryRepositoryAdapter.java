package com.serviloc.categories.infrastructure.persistence;

import com.serviloc.categories.domain.model.ServiceCategory;
import com.serviloc.categories.domain.repository.CategoryRepository;
import com.serviloc.categories.infrastructure.persistence.CategoryJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository repo;

    public CategoryRepositoryAdapter(CategoryJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<ServiceCategory> findAll() {
        return repo.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<ServiceCategory> findBySlug(String slug) {
        return repo.findBySlug(slug).map(this::toDomain);
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
        return ServiceCategory.reconstitute(
                e.getId(), e.getSlug(), e.getLabel(),
                e.getIconKey(), e.getColor(),
                e.getDemandCount(), e.getPercentageShare()
        );
    }

    private CategoryJpaEntity toEntity(ServiceCategory c) {
        CategoryJpaEntity e = new CategoryJpaEntity();
        e.setId(c.getId());
        e.setSlug(c.getSlug());
        e.setLabel(c.getLabel());
        e.setIconKey(c.getIconKey());
        e.setColor(c.getColor());
        e.setDemandCount(c.getDemandCount());
        e.setPercentageShare(c.getPercentageShare());
        return e;
    }
}

