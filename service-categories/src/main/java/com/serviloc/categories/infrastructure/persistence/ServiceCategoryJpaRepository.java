package com.serviloc.categories.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ServiceCategoryJpaRepository extends JpaRepository<ServiceCategoryJpaEntity, String> {

    Optional<ServiceCategoryJpaEntity> findByLabelIgnoreCase(String label);

    boolean existsByLabelIgnoreCase(String label);

    @Query("SELECT COALESCE(SUM(c.demandCount), 0) FROM ServiceCategoryJpaEntity c")
    long sumDemandCount();
}
