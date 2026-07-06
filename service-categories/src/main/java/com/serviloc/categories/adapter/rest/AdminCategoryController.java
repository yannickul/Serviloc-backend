package com.serviloc.categories.adapter.rest;

import com.serviloc.categories.application.dto.CategoryDto;
import com.serviloc.categories.application.dto.CategoryResponseDto;
import com.serviloc.categories.application.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/admin/categories")
@Tag(
        name = "Admin - Categories",
        description = "Endpoints d'administration pour la gestion des catégories"
)
public class AdminCategoryController {

    private final CategoryService service;

    public AdminCategoryController(CategoryService service) {
        this.service = service;
    }

    /** Liste des catégories avec stats */
    @GetMapping
    @Operation(
            summary = "Lister toutes les catégories (admin)",
            description = "Retourne toutes les catégories avec leurs statistiques d'utilisation."
    )
    @ApiResponse(responseCode = "200", description = "Liste retournée avec succès")
    public List<CategoryResponseDto> listCategoriesForAdmin() {
        return service.getAllForAdmin();
    }

    /** Création d’une catégorie */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Créer une nouvelle catégorie",
            description = "Ajoute une nouvelle catégorie dans le système."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Catégorie créée"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public CategoryResponseDto createCategory(@RequestBody CategoryDto dto) {
        return service.create(dto);
    }

    /** Mise à jour par slug */
    @PatchMapping("/{slug}")
    @Operation(
            summary = "Mettre à jour une catégorie",
            description = "Met à jour une catégorie existante identifiée par son slug."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catégorie mise à jour"),
            @ApiResponse(responseCode = "404", description = "Catégorie introuvable")
    })
    public CategoryResponseDto updateCategory(
            @PathVariable String slug,
            @RequestBody CategoryDto dto
    ) {
        return service.updateBySlug(slug, dto);
    }

    /** Suppression par slug */
    @DeleteMapping("/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Supprimer une catégorie",
            description = "Supprime une catégorie identifiée par son slug."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Catégorie supprimée"),
            @ApiResponse(responseCode = "404", description = "Catégorie introuvable")
    })
    public void deleteCategory(@PathVariable String slug) {
        service.deleteBySlug(slug);
    }
}
