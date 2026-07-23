package com.serviloc.categories.application.mapper;

import com.serviloc.categories.application.dto.BudgetRangeResponse;
import com.serviloc.categories.application.dto.CategoryResponse;
import com.serviloc.categories.application.dto.ClientCategoryResponse;
import com.serviloc.categories.domain.model.ServiceCategory;

public final class CategoryDtoMapper {

    private CategoryDtoMapper() {
    }

    /** Vue complète (admin/internal), inclut demandCount/percentageShare. */
    public static CategoryResponse toResponse(ServiceCategory category, long totalDemandCount) {
        return new CategoryResponse(
                category.getId().value(),
                category.getLabel(),
                category.getIconKey().toWireFormat(),
                category.getDescription(),
                category.getColor(),
                toBudgetRangeResponse(category),
                category.getDemandCount(),
                roundToOneDecimal(category.percentageShareOver(totalDemandCount))
        );
    }

    /** Vue publique (GET /client/categories), sans statistiques. */
    public static ClientCategoryResponse toClientResponse(ServiceCategory category) {
        return new ClientCategoryResponse(
                category.getId().value(),
                category.getLabel(),
                category.getIconKey().toWireFormat(),
                category.getDescription(),
                category.getColor(),
                toBudgetRangeResponse(category)
        );
    }

    private static BudgetRangeResponse toBudgetRangeResponse(ServiceCategory category) {
        return new BudgetRangeResponse(category.getBudgetRange().min(), category.getBudgetRange().max());
    }

    private static double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
