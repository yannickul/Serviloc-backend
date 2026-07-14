package com.serviloc.categories.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * Forme exacte du schéma {@code ServiceCategory} défini dans API_CONTRACT.md §4.16.
 * Implémente Serializable car mis en cache Redis (sérialisation JDK ou JSON selon config).
 */
@Schema(name = "ServiceCategory", description = "Catégorie de service du référentiel ServiLoc")
public record CategoryResponse(

        @Schema(example = "cat_plomberie", accessMode = Schema.AccessMode.READ_ONLY)
        String id,

        @Schema(example = "Plomberie")
        String label,

        @Schema(example = "wrench", allowableValues = {"wrench", "bolt", "broom", "key", "brush", "plus", "leaf"})
        String iconKey,

        @Schema(example = "#dbeafe")
        String color,

        @Schema(example = "47", accessMode = Schema.AccessMode.READ_ONLY)
        long demandCount,

        @Schema(example = "34.0", accessMode = Schema.AccessMode.READ_ONLY)
        double percentageShare

) implements Serializable {
}
