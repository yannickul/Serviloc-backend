package com.serviloc.categories.application;

import com.serviloc.categories.application.dto.CategoryResponse;
import com.serviloc.categories.application.dto.CategoryUpsertRequest;
import com.serviloc.categories.application.service.CategoryService;
import com.serviloc.categories.domain.exception.CategoryNotFoundException;
import com.serviloc.categories.domain.exception.DuplicateCategoryLabelException;
import com.serviloc.categories.domain.model.CategoryId;
import com.serviloc.categories.domain.model.IconKey;
import com.serviloc.categories.domain.model.ServiceCategory;
import com.serviloc.categories.domain.repository.ServiceCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    ServiceCategoryRepository repository;

    CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(repository);
    }

    @Test
    void shouldCreateCategoryWhenLabelDoesNotExist() {
        var request = new CategoryUpsertRequest("Jardinage", "leaf", "#d1fae5");
        when(repository.existsByLabelIgnoreCase("Jardinage")).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.totalDemandCount()).thenReturn(0L);

        CategoryResponse response = categoryService.create(request);

        assertThat(response.id()).isEqualTo("cat_jardinage");
        assertThat(response.iconKey()).isEqualTo("leaf");
    }

    @Test
    void shouldRejectDuplicateLabelOnCreate() {
        var request = new CategoryUpsertRequest("Plomberie", "wrench", "#dbeafe");
        when(repository.existsByLabelIgnoreCase("Plomberie")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(DuplicateCategoryLabelException.class);
    }

    @Test
    void shouldThrowWhenIncrementingUnknownCategory() {
        when(repository.findById(CategoryId.of("cat_inconnue"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.incrementDemandCount("cat_inconnue"))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void shouldListCategoriesWithComputedPercentageShare() {
        ServiceCategory plomberie = ServiceCategory.create("Plomberie", IconKey.WRENCH, "#dbeafe");
        plomberie.incrementDemandCount();
        plomberie.incrementDemandCount();
        when(repository.findAll()).thenReturn(List.of(plomberie));
        when(repository.totalDemandCount()).thenReturn(4L);

        List<CategoryResponse> result = categoryService.listForAdmin();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).percentageShare()).isEqualTo(50.0);
    }
}
