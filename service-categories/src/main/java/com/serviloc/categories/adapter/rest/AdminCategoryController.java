package com.serviloc.categories.adapter.rest;

import com.serviloc.categories.application.dto.CategoryDto;
import com.serviloc.categories.application.dto.CategoryResponseDto;
import com.serviloc.categories.application.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/admin/categories")
public class AdminCategoryController {

    private final CategoryService service;

    public AdminCategoryController(CategoryService service) {
        this.service = service;
    }

    /** Liste des catégories avec stats */
    @GetMapping
    public List<CategoryResponseDto> listCategoriesForAdmin() {
        return service.getAllForAdmin();
    }

    /** Création d’une catégorie */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDto createCategory(@RequestBody CategoryDto dto) {
        return service.create(dto);
    }

    /** Mise à jour par slug */
    @PatchMapping("/{slug}")
    public CategoryResponseDto updateCategory(@PathVariable String slug, @RequestBody CategoryDto dto) {
        return service.updateBySlug(slug, dto);
    }

    /** Suppression par slug */
    @DeleteMapping("/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable String slug) {
        service.deleteBySlug(slug);
    }
}
