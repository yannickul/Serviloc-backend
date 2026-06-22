package com.serviloc.categories.adapter.rest;

import com.serviloc.categories.application.dto.CategoryResponseDto;
import com.serviloc.categories.application.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/client/categories")
@Tag(
        name = "Client - Categories",
        description = "Endpoints publics pour les clients (catégories sans statistiques)"
)
public class ClientCategoryController {

    private final CategoryService service;

    public ClientCategoryController(CategoryService service) {
        this.service = service;
    }

    /** Liste des catégories côté client (sans stats) */
    @GetMapping
    @Operation(
            summary = "Lister les catégories (client)",
            description = "Retourne la liste des catégories visibles côté client, sans statistiques."
    )
    @ApiResponse(responseCode = "200", description = "Liste retournée avec succès")
    public List<CategoryResponseDto> listCategories() {
        return service.getAllForClient();
    }
}
