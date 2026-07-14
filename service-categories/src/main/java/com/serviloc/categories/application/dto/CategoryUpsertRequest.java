package com.serviloc.categories.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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

        @NotBlank(message = "La couleur est obligatoire")
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "La couleur doit être un code hexadécimal (ex: #d1fae5)")
        @Schema(example = "#d1fae5", requiredMode = Schema.RequiredMode.REQUIRED)
        String color

) {
}
