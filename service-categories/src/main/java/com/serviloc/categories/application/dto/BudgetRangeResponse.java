package com.serviloc.categories.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(name = "BudgetRange")
public record BudgetRangeResponse(
        @Schema(example = "5000") int min,
        @Schema(example = "50000") int max
) implements Serializable {
}
