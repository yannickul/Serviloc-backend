package com.serviloc.categories.domain.repository;

import com.serviloc.categories.domain.model.ServiceCategory;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    List<ServiceCategory> findAll();
    Optional<ServiceCategory> findBySlug(String slug);
    ServiceCategory save(ServiceCategory category);
    void delete(Long id);
}

