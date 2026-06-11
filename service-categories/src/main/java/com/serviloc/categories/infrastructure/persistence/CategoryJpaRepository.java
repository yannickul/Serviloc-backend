package com.serviloc.categories.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {

    /** Recherche par slug (identifiant public) */
    Optional<CategoryJpaEntity> findBySlug(String slug);
}
