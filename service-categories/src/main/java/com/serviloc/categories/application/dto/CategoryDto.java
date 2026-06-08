package com.serviloc.categories.application.dto;

import com.serviloc.categories.domain.model.ServiceCategory;

public record CategoryDto(
        String label,
        String iconKey,
        String color
) {
    public ServiceCategory toDomain() {
        return new ServiceCategory(null, label, iconKey, color, 0, 0.0);
    }

    public ServiceCategory toDomainWithId(Long id) {
        return new ServiceCategory(id, label, iconKey, color, 0, 0.0);
    }
}
