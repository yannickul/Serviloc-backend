package com.serviloc.categories.domain.repository;

import com.serviloc.categories.domain.model.ServiceCategory;
import java.util.List;

public interface CategoryRepository {
    List<ServiceCategory> findAll();
    ServiceCategory findById(Long id);
    ServiceCategory save(ServiceCategory category);
    void delete(Long id);
}
