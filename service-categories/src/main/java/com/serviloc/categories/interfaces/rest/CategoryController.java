package com.serviloc.categories.interfaces.rest;

import com.serviloc.categories.infrastructure.persistence.CategoryJpaEntity;
import com.serviloc.categories.infrastructure.persistence.CategoryJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@Tag(
        name = "Public - Categories",
        description = "Endpoints publics pour consulter les catégories"
)
public class CategoryController {

    private final CategoryJpaRepository categoryRepository;

    public CategoryController(CategoryJpaRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // GET all
    @GetMapping
    @Operation(
            summary = "Lister toutes les catégories",
            description = "Retourne la liste complète des catégories disponibles."
    )
    @ApiResponse(responseCode = "200", description = "Liste retournée avec succès")
    public ResponseEntity<List<CategoryJpaEntity>> getAllCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    // GET by slug
    @GetMapping("/{slug}")
    @Operation(
            summary = "Obtenir une catégorie par slug",
            description = "Retourne une catégorie correspondant au slug fourni."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catégorie trouvée"),
            @ApiResponse(responseCode = "404", description = "Catégorie introuvable")
    })
    public ResponseEntity<CategoryJpaEntity> getCategoryBySlug(@PathVariable String slug) {
        return categoryRepository.findBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST create
    @PostMapping
    @Operation(
            summary = "Créer une catégorie",
            description = "Ajoute une nouvelle catégorie dans la base."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catégorie créée"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<CategoryJpaEntity> createCategory(@RequestBody CategoryJpaEntity category) {
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    // PUT update by slug
    @PutMapping("/{slug}")
    @Operation(
            summary = "Mettre à jour une catégorie",
            description = "Met à jour une catégorie existante identifiée par son slug."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catégorie mise à jour"),
            @ApiResponse(responseCode = "404", description = "Catégorie introuvable")
    })
    public ResponseEntity<CategoryJpaEntity> updateCategory(
            @PathVariable String slug,
            @RequestBody CategoryJpaEntity category
    ) {
        return categoryRepository.findBySlug(slug)
                .map(existing -> {
                    category.setId(existing.getId()); // garder l’ID interne
                    return ResponseEntity.ok(categoryRepository.save(category));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE by slug
    @DeleteMapping("/{slug}")
    @Operation(
            summary = "Supprimer une catégorie",
            description = "Supprime une catégorie identifiée par son slug."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Catégorie supprimée"),
            @ApiResponse(responseCode = "404", description = "Catégorie introuvable")
    })
    public ResponseEntity<?> deleteCategory(@PathVariable String slug) {
        return categoryRepository.findBySlug(slug)
                .map(existing -> {
                    categoryRepository.delete(existing);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
