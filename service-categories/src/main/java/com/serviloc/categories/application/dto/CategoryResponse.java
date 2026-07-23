package com.serviloc.categories.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * Forme complète d'une catégorie, utilisée par /admin/categories et /internal/**
 * (inclut les statistiques demandCount/percentageShare, réservées à un usage interne —
 * voir API_CONTRACT_INTERNAL.md). La forme publique exposée par /client/categories est
 * {@link ClientCategoryResponse}, structurellement différente.
 */
@Schema(name = "ServiceCategory", description = "Catégorie de service (vue complète, admin/internal)")
public record CategoryResponse(

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

        BudgetRangeResponse budgetRange,

        @Schema(example = "47", accessMode = Schema.AccessMode.READ_ONLY)
        long demandCount,

        @Schema(example = "34.0", accessMode = Schema.AccessMode.READ_ONLY)
        double percentageShare

) implements Serializable {
}
