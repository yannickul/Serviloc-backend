package com.serviloc.categories.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CategoryDeletedResponse")
public record CategoryDeletedResponse(
        @Schema(example = "cat_jardinage") String categoryId,
        @Schema(example = "true") boolean deleted
) {
}
