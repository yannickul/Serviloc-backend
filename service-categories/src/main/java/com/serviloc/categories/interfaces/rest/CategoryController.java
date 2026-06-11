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

    @GetMapping
    public ResponseEntity<List<CategoryJpaEntity>> getAllCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryJpaEntity> getCategoryById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CategoryJpaEntity> createCategory(@RequestBody CategoryJpaEntity category) {
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryJpaEntity> updateCategory(@PathVariable Long id,
                                                            @RequestBody CategoryJpaEntity category) {
        return categoryRepository.findById(id)
                .map(existing -> {
                    category.setId(id);
                    return ResponseEntity.ok(categoryRepository.save(category));
                })
                .orElse(ResponseEntity.notFound().build());
    }

   @DeleteMapping("/{id}")
public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
    return categoryRepository.findById(id)
            .map(existing -> {
                categoryRepository.delete(existing);
                return ResponseEntity.noContent().build();
            })
            .orElse(ResponseEntity.notFound().build());
}

}

