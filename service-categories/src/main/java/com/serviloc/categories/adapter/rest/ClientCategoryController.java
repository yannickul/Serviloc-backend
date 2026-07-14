package com.serviloc.categories.adapter.rest;

import com.serviloc.categories.adapter.rest.dto.ApiResponse;
import com.serviloc.categories.application.dto.CategoryResponse;
import com.serviloc.categories.application.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints publics côté client (role: "client").
 * Le préfixe /client/** est contrôlé en amont par le Gateway (RoleAuthFilter).
 */
@RestController
@Tag(name = "Client - Catégories", description = "Consultation du référentiel des catégories de services")
public class ClientCategoryController {

    private final CategoryService categoryService;

    public ClientCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Liste des catégories de services disponibles",
            description = "Réponse mise en cache Redis (TTL 1h). Voir API_CONTRACT.md §6.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Liste des catégories")
    })
    @GetMapping("/client/categories")
    public ApiResponse<List<CategoryResponse>> getClientCategories() {
        return ApiResponse.of(categoryService.listForClient());
    }
}
