package com.serviloc.categories.domain;

import com.serviloc.categories.domain.model.BudgetRange;
import com.serviloc.categories.domain.model.CategoryId;
import com.serviloc.categories.domain.model.IconKey;
import com.serviloc.categories.domain.model.ServiceCategory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceCategoryTest {

    private static final BudgetRange DEFAULT_BUDGET = new BudgetRange(5000, 50000);
    private static final String DEFAULT_DESCRIPTION = "Description de test suffisamment longue";

    @Test
    void shouldCreateCategoryWithGeneratedSlugId() {
        ServiceCategory category = ServiceCategory.create(
                "Plomberie", IconKey.WRENCH, DEFAULT_DESCRIPTION, "#dbeafe", DEFAULT_BUDGET);

        assertThat(category.getId()).isEqualTo(CategoryId.of("cat_plomberie"));
        assertThat(category.getDemandCount()).isZero();
        assertThat(category.getBudgetRange()).isEqualTo(DEFAULT_BUDGET);
    }

    @Test
    void shouldRejectBlankLabel() {
        assertThatThrownBy(() -> ServiceCategory.create(
                "  ", IconKey.WRENCH, DEFAULT_DESCRIPTION, "#dbeafe", DEFAULT_BUDGET))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectInvalidColor() {
        assertThatThrownBy(() -> ServiceCategory.create(
                "Peinture", IconKey.BRUSH, DEFAULT_DESCRIPTION, "not-a-color", DEFAULT_BUDGET))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectBlankDescription() {
        assertThatThrownBy(() -> ServiceCategory.create(
                "Peinture", IconKey.BRUSH, "  ", "#ffe4e6", DEFAULT_BUDGET))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectInvalidBudgetRange() {
        assertThatThrownBy(() -> new BudgetRange(50000, 5000))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldIncrementDemandCount() {
        ServiceCategory category = ServiceCategory.create(
                "Jardinage", IconKey.LEAF, DEFAULT_DESCRIPTION, "#d1fae5", new BudgetRange(3000, 30000));

        category.incrementDemandCount();
        category.incrementDemandCount();

        assertThat(category.getDemandCount()).isEqualTo(2L);
    }

    @Test
    void shouldComputePercentageShare() {
        ServiceCategory category = ServiceCategory.create(
                "Électricité", IconKey.BOLT, DEFAULT_DESCRIPTION, "#fef9c3", new BudgetRange(5000, 60000));
        category.incrementDemandCount();
        category.incrementDemandCount();
        category.incrementDemandCount();

        double share = category.percentageShareOver(12);

        assertThat(share).isEqualTo(25.0);
    }

    @Test
    void shouldReturnZeroPercentageWhenNoTotalDemand() {
        ServiceCategory category = ServiceCategory.create(
                "Serrurerie", IconKey.KEY, DEFAULT_DESCRIPTION, "#e0e7ff", new BudgetRange(5000, 40000));

        assertThat(category.percentageShareOver(0)).isZero();
    }
}
