package com.serviloc.categories.adapter.rest;

import com.serviloc.categories.adapter.rest.dto.ApiResponse;
import com.serviloc.categories.application.dto.CategoryDeletedResponse;
import com.serviloc.categories.application.dto.CategoryResponse;
import com.serviloc.categories.application.dto.CategoryUpsertRequest;
import com.serviloc.categories.application.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints d'administration (role: "admin"), gérés en amont par le Gateway.
 * Toute mutation invalide le cache Redis utilisé par GET /client/categories.
 */
@RestController
@Tag(name = "Admin - Catégories", description = "CRUD du référentiel des catégories de services")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Liste complète des catégories avec statistiques")
    @GetMapping("/admin/categories")
    public ApiResponse<List<CategoryResponse>> getAdminCategories() {
        return ApiResponse.of(categoryService.listForAdmin());
    }

    @Operation(summary = "Création d'une catégorie")
    @PostMapping("/admin/categories")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryUpsertRequest request) {
        CategoryResponse created = categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(created));
    }

    @Operation(summary = "Mise à jour d'une catégorie")
    @PutMapping("/admin/categories/{categoryId}")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable String categoryId,
            @Valid @RequestBody CategoryUpsertRequest request) {
        return ApiResponse.of(categoryService.update(categoryId, request));
    }

    @Operation(summary = "Suppression d'une catégorie", description = "Invalide également le cache Redis client.")
    @DeleteMapping("/admin/categories/{categoryId}")
    public ApiResponse<CategoryDeletedResponse> deleteCategory(@PathVariable String categoryId) {
        return ApiResponse.of(categoryService.delete(categoryId));
    }
}
