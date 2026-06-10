package com.serviloc.categories.infrastructure.persistence;

import com.serviloc.categories.domain.model.ServiceCategory;
import com.serviloc.categories.domain.repository.CategoryRepository;
import com.serviloc.categories.domain.exception.CategoryNotFoundException;
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
        return repo.findAll().stream()
                .map(this::toDomain)
                .toList();
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

    /** Conversion JPA → Domaine */
    private ServiceCategory toDomain(CategoryJpaEntity e) {
        return ServiceCategory.reconstitute(
                e.getId(),          // interne
                e.getSlug(),        // identifiant public
                e.getLabel(),
                e.getIconKey(),
                e.getColor(),
                e.getDemandCount(),
                e.getPercentageShare()
        );
    }

    /** Conversion Domaine → JPA */
    private CategoryJpaEntity toEntity(ServiceCategory c) {
        CategoryJpaEntity e = new CategoryJpaEntity();
        e.setId(c.getId());          // interne
        e.setSlug(c.getSlug());      // exposé comme "id" côté API
        e.setLabel(c.getLabel());
        e.setIconKey(c.getIconKey());
        e.setColor(c.getColor());
        e.setDemandCount(c.getDemandCount());
        e.setPercentageShare(c.getPercentageShare());
        return e;
    }
}
