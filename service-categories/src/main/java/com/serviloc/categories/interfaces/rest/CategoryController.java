package com.serviloc.categories.interfaces.rest;

import com.serviloc.categories.infrastructure.persistence.CategoryJpaEntity;
import com.serviloc.categories.infrastructure.persistence.CategoryJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryJpaRepository categoryRepository;

    public CategoryController(CategoryJpaRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // GET all
    @GetMapping
    public ResponseEntity<List<CategoryJpaEntity>> getAllCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    // GET by slug
    @GetMapping("/{slug}")
    public ResponseEntity<CategoryJpaEntity> getCategoryBySlug(@PathVariable String slug) {
        return categoryRepository.findBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST create
    @PostMapping
    public ResponseEntity<CategoryJpaEntity> createCategory(@RequestBody CategoryJpaEntity category) {
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    // PUT update by slug
    @PutMapping("/{slug}")
    public ResponseEntity<CategoryJpaEntity> updateCategory(@PathVariable String slug,
                                                            @RequestBody CategoryJpaEntity category) {
        return categoryRepository.findBySlug(slug)
                .map(existing -> {
                    category.setId(existing.getId()); // garder l’ID interne
                    return ResponseEntity.ok(categoryRepository.save(category));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE by slug
    @DeleteMapping("/{slug}")
    public ResponseEntity<?> deleteCategory(@PathVariable String slug) {
        return categoryRepository.findBySlug(slug)
                .map(existing -> {
                    categoryRepository.delete(existing);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}

