package com.serviloc.categories.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * Forme publique de ServiceCategory exposée par GET /client/categories.
 * Ne contient volontairement pas demandCount/percentageShare (réservés à un usage
 * interne / admin, voir CategoryResponse) — décision alignée avec la Gateway,
 * qui les recompose différemment pour admin/dashboard.popularCategories via
 * GET /internal/categories/stats.
 */
@Schema(name = "ClientServiceCategory", description = "Catégorie de service — vue publique client")
public record ClientCategoryResponse(

        @Schema(example = "cat_plomberie", accessMode = Schema.AccessMode.READ_ONLY)
        String id,

        @Schema(example = "Plomberie")
        String label,

        @Schema(example = "wrench", allowableValues = {"wrench", "bolt", "broom", "key", "brush", "plus", "leaf"})
        String iconKey,

        @Schema(example = "Réparation de fuites, installation sanitaire, dépannage plomberie")
        String description,

        @Schema(example = "#dbeafe")
        String color,

        BudgetRangeResponse budgetRange

) implements Serializable {
}
