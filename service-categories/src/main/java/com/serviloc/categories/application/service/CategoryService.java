package com.serviloc.categories.application.service;

import com.serviloc.categories.application.dto.CategoryDto;
import com.serviloc.categories.application.dto.CategoryResponseDto;
import com.serviloc.categories.domain.exception.CategoryNotFoundException;
import com.serviloc.categories.domain.model.ServiceCategory;
import com.serviloc.categories.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    /** Liste côté client (sans stats) */
    public List<CategoryResponseDto> getAllForClient() {
        return repository.findAll().stream()
                .map(CategoryResponseDto::from)
                .toList();
    }

    /** Liste côté admin (avec stats calculées ailleurs) */
    public List<CategoryResponseDto> getAllForAdmin() {
        return repository.findAll().stream()
                .map(cat -> CategoryResponseDto.withStats(cat, 47, 34)) // stats mockées
                .toList();
    }

    /** Création d’une catégorie */
    public CategoryResponseDto create(CategoryDto dto) {
        ServiceCategory category = dto.toDomain();
        ServiceCategory saved = repository.save(category);
        return CategoryResponseDto.from(saved);
    }

    /** Mise à jour par slug */
    public CategoryResponseDto updateBySlug(String slug, CategoryDto dto) {
        ServiceCategory existing = repository.findBySlug(slug)
                .orElseThrow(() -> new CategoryNotFoundException(slug));
        existing.updateDetails(dto.label(), dto.iconKey(), dto.color());
        ServiceCategory updated = repository.save(existing);
        return CategoryResponseDto.from(updated);
    }

    /** Suppression par slug */
    public void deleteBySlug(String slug) {
        ServiceCategory existing = repository.findBySlug(slug)
                .orElseThrow(() -> new CategoryNotFoundException(slug));
        repository.delete(existing.getId()); // interne
    }
}
