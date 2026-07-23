package com.serviloc.categories.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Corps de requête pour POST /admin/categories et PUT /admin/categories/:id
 * (structure identique, voir API_CONTRACT.md §8).
 */
@Schema(name = "CategoryUpsertRequest")
public record CategoryUpsertRequest(

        @NotBlank(message = "Le label est obligatoire")
        @Size(max = 100, message = "Le label ne peut pas dépasser 100 caractères")
        @Schema(example = "Jardinage", requiredMode = Schema.RequiredMode.REQUIRED)
        String label,

        @NotBlank(message = "iconKey est obligatoire")
        @Pattern(regexp = "wrench|bolt|broom|key|brush|plus|leaf",
                message = "iconKey doit être l'une des valeurs : wrench, bolt, broom, key, brush, plus, leaf")
        @Schema(example = "leaf", requiredMode = Schema.RequiredMode.REQUIRED)
        String iconKey,

        @NotBlank(message = "La description est obligatoire")
        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
        @Schema(example = "Entretien de jardins, tonte, taille de haies, plantations",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String description,

        @NotBlank(message = "La couleur est obligatoire")
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "La couleur doit être un code hexadécimal (ex: #d1fae5)")
        @Schema(example = "#d1fae5", requiredMode = Schema.RequiredMode.REQUIRED)
        String color,

        @NotNull(message = "budgetRange est obligatoire")
        @Valid
        BudgetRangeRequest budgetRange

) {

    @Schema(name = "BudgetRangeRequest")
    public record BudgetRangeRequest(

            @jakarta.validation.constraints.PositiveOrZero(message = "Le budget minimum ne peut pas être négatif")
            @Schema(example = "5000", requiredMode = Schema.RequiredMode.REQUIRED)
            Integer min,

            @jakarta.validation.constraints.PositiveOrZero(message = "Le budget maximum ne peut pas être négatif")
            @Schema(example = "50000", requiredMode = Schema.RequiredMode.REQUIRED)
            Integer max

    ) {
    }
}
