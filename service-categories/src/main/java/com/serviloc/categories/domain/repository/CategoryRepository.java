package com.serviloc.categories.domain.repository;

import com.serviloc.categories.domain.model.ServiceCategory;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    /** Liste complète des catégories */
    List<ServiceCategory> findAll();

    /** Recherche par slug (identifiant public) */
    Optional<ServiceCategory> findBySlug(String slug);

    /** Sauvegarde ou mise à jour */
    ServiceCategory save(ServiceCategory category);

    /** Suppression par id interne (utilisé uniquement par l’adapter) */
    void delete(Long id);
}
