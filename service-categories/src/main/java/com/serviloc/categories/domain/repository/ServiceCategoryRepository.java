package com.serviloc.categories.domain.repository;

import com.serviloc.categories.domain.model.CategoryId;
import com.serviloc.categories.domain.model.ServiceCategory;

import java.util.List;
import java.util.Optional;

/**
 * Port (interface) du domaine pour la persistance des catégories.
 * Implémenté dans la couche infrastructure (adapter JPA).
 */
public interface ServiceCategoryRepository {

    List<ServiceCategory> findAll();

    Optional<ServiceCategory> findById(CategoryId id);

    Optional<ServiceCategory> findByLabelIgnoreCase(String label);

    boolean existsByLabelIgnoreCase(String label);

    ServiceCategory save(ServiceCategory category);

    void deleteById(CategoryId id);

    boolean existsById(CategoryId id);

    /** Somme du demandCount de toutes les catégories, utilisée pour calculer percentageShare. */
    long totalDemandCount();
}
