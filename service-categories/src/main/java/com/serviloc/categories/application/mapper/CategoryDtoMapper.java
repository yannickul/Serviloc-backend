package com.serviloc.categories.application.mapper;

import com.serviloc.categories.application.dto.CategoryResponse;
import com.serviloc.categories.domain.model.ServiceCategory;

public final class CategoryDtoMapper {

    private CategoryDtoMapper() {
    }

    public static CategoryResponse toResponse(ServiceCategory category, long totalDemandCount) {
        double share = category.percentageShareOver(totalDemandCount);
        double rounded = Math.round(share * 10.0) / 10.0;
        return new CategoryResponse(
                category.getId().value(),
                category.getLabel(),
                category.getIconKey().toWireFormat(),
                category.getColor(),
                category.getDemandCount(),
                rounded
        );
    }
}
