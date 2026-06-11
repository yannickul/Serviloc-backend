package com.serviloc.categories.application.controller;

import com.serviloc.categories.application.dto.CategoryDto;
import com.serviloc.categories.application.dto.CategoryResponseDto;
import com.serviloc.categories.application.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping("/client/categories")
    public List<CategoryResponseDto> listCategoriesForClient() {
        return service.getAllForClient();
    }

    @GetMapping("/admin/categories")
    public List<CategoryResponseDto> listCategoriesForAdmin() {
        return service.getAllForAdmin();
    }

    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDto createCategory(@RequestBody CategoryDto dto) {
        return service.create(dto);
    }

    /** Mise à jour par slug (identifiant public) */
    @PatchMapping("/admin/categories/{slug}")
    public CategoryResponseDto updateCategory(@PathVariable String slug, @RequestBody CategoryDto dto) {
        return service.updateBySlug(slug, dto);
    }

    /** Suppression par slug (identifiant public) */
    @DeleteMapping("/admin/categories/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable String slug) {
        service.deleteBySlug(slug);
    }
}
