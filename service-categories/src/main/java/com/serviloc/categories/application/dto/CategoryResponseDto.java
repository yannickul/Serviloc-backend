package com.serviloc.categories.application.dto;

import com.serviloc.categories.domain.model.ServiceCategory;

public record CategoryResponseDto(
        String id,              // correspond au slug exposé dans l’API
        String label,
        String iconKey,
        String color,
        Integer demandCount,
        Integer percentageShare
) {
    /** DTO sans stats (client) */
    public static CategoryResponseDto from(ServiceCategory category) {
        return new CategoryResponseDto(
                category.getSlug(),   // slug exposé comme "id"
                category.getLabel(),
                category.getIconKey(),
                category.getColor(),
                null,
                null
        );
    }

    /** DTO avec stats (admin) */
    public static CategoryResponseDto withStats(ServiceCategory category,
                                                int demandCount,
                                                int percentageShare) {
        return new CategoryResponseDto(
                category.getSlug(),
                category.getLabel(),
                category.getIconKey(),
                category.getColor(),
                demandCount,
                percentageShare
        );
    }
}

