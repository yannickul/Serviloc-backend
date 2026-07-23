package com.serviloc.categories.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CategoryIncrementResponse")
public record CategoryIncrementResponse(
        @Schema(example = "cat_plomberie") String categoryId,
        @Schema(example = "48") long demandCount
) {
}
