package com.serviloc.categories.domain;

import com.serviloc.categories.domain.model.CategoryId;
import com.serviloc.categories.domain.model.IconKey;
import com.serviloc.categories.domain.model.ServiceCategory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceCategoryTest {

    @Test
    void shouldCreateCategoryWithGeneratedSlugId() {
        ServiceCategory category = ServiceCategory.create("Plomberie", IconKey.WRENCH, "#dbeafe");

        assertThat(category.getId()).isEqualTo(CategoryId.of("cat_plomberie"));
        assertThat(category.getDemandCount()).isZero();
    }

    @Test
    void shouldRejectBlankLabel() {
        assertThatThrownBy(() -> ServiceCategory.create("  ", IconKey.WRENCH, "#dbeafe"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectInvalidColor() {
        assertThatThrownBy(() -> ServiceCategory.create("Peinture", IconKey.BRUSH, "not-a-color"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldIncrementDemandCount() {
        ServiceCategory category = ServiceCategory.create("Jardinage", IconKey.LEAF, "#d1fae5");

        category.incrementDemandCount();
        category.incrementDemandCount();

        assertThat(category.getDemandCount()).isEqualTo(2L);
    }

    @Test
    void shouldComputePercentageShare() {
        ServiceCategory category = ServiceCategory.create("Électricité", IconKey.BOLT, "#fef9c3");
        category.incrementDemandCount();
        category.incrementDemandCount();
        category.incrementDemandCount();

        double share = category.percentageShareOver(12);

        assertThat(share).isEqualTo(25.0);
    }

    @Test
    void shouldReturnZeroPercentageWhenNoTotalDemand() {
        ServiceCategory category = ServiceCategory.create("Serrurerie", IconKey.KEY, "#e0e7ff");

        assertThat(category.percentageShareOver(0)).isZero();
    }
}
