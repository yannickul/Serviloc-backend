package com.serviloc.categories.infrastructure.persistence;

import com.serviloc.categories.infrastructure.persistence.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {
    Optional<CategoryJpaEntity> findBySlug(String slug);
}

