package com.serviloc.categories.adapter.rest;

import com.serviloc.categories.adapter.rest.dto.ApiResponse;
import com.serviloc.categories.application.dto.CategoryIncrementResponse;
import com.serviloc.categories.application.dto.CategoryResponse;
import com.serviloc.categories.application.service.CategoryService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints internes, réservés aux appels Feign inter-services (Service Missions,
 * Service Utilisateurs). Jamais exposés par le Gateway ; protégés par
 * {@link com.serviloc.categories.infrastructure.security.InternalTokenFilter}
 * via le header X-Internal-Token (voir ARCHITECTURE §4.4).
 */
@RestController
@Tag(name = "Internal - Catégories", description = "Appels inter-services (Feign) uniquement")
@Hidden
public class InternalCategoryController {

    private final CategoryService categoryService;

    public InternalCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Liste toutes les catégories (sans cache)", description = "Consommé par Service Missions")
    @GetMapping("/internal/categories")
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        return ApiResponse.of(categoryService.listForInternal());
    }

    @Operation(summary = "Détail d'une catégorie par ID",
            description = "Consommé par Service Missions et Service Utilisateurs")
    @GetMapping("/internal/categories/{id}")
    public ApiResponse<CategoryResponse> getCategoryById(@PathVariable String id) {
        return ApiResponse.of(categoryService.getById(id));
    }

    @Operation(summary = "Recherche par label",
            description = "Consommé par Service Utilisateurs (validation prestataire)")
    @GetMapping("/internal/categories/label/{label}")
    public ApiResponse<CategoryResponse> getCategoryByLabel(@PathVariable String label) {
        return ApiResponse.of(categoryService.getByLabel(label));
    }

    @Operation(summary = "Statistiques par catégorie (demandCount, percentageShare)",
            description = "Consommé par Service Missions (admin/stats)")
    @GetMapping("/internal/categories/stats")
    public ApiResponse<List<CategoryResponse>> getStats() {
        return ApiResponse.of(categoryService.getStats());
    }

    @Operation(summary = "Incrémente demandCount d'une catégorie",
            description = "Déclenché normalement via le consumer RabbitMQ demand.published ; "
                    + "endpoint conservé pour rejouer manuellement / tests d'intégration Service Missions")
    @PutMapping("/internal/categories/{id}/increment")
    public ApiResponse<CategoryIncrementResponse> incrementDemandCount(@PathVariable String id) {
        return ApiResponse.of(categoryService.incrementDemandCount(id));
    }
}
