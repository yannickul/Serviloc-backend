package com.serviloc.categories.domain.model;

public record ServiceCategory(
        Long id,
        String label,
        String iconKey,
        String color,
        Integer demandCount,
        Double percentageShare
) {}

