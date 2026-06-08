package com.serviloc.categories.application.service;

import com.serviloc.categories.application.dto.CategoryDto;
import com.serviloc.categories.domain.model.ServiceCategory;
import com.serviloc.categories.domain.repository.CategoryRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    @Cacheable("categories:all")
    public List<ServiceCategory> getAllForClient() {
        return repository.findAll();
    }

    public List<ServiceCategory> getAllForAdmin() {
        return repository.findAll();
    }

    @CacheEvict(value = "categories:all", allEntries = true)
    public ServiceCategory create(CategoryDto dto) {
        return repository.save(dto.toDomain());
    }

    @CacheEvict(value = "categories:all", allEntries = true)
    public ServiceCategory update(Long id, CategoryDto dto) {
        return repository.save(dto.toDomainWithId(id));
    }

    @CacheEvict(value = "categories:all", allEntries = true)
    public void delete(Long id) {
        repository.delete(id);
    }
}
