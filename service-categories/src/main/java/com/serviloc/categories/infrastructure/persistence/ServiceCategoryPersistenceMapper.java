package com.serviloc.categories.infrastructure.persistence;

import com.serviloc.categories.domain.model.CategoryId;
import com.serviloc.categories.domain.model.IconKey;
import com.serviloc.categories.domain.model.ServiceCategory;
import org.springframework.stereotype.Component;

@Component
public class ServiceCategoryPersistenceMapper {

    public ServiceCategoryJpaEntity toJpaEntity(ServiceCategory category) {
        return new ServiceCategoryJpaEntity(
                category.getId().value(),
                category.getLabel(),
                ServiceCategoryJpaEntity.IconKeyJpa.valueOf(category.getIconKey().name()),
                category.getColor(),
                category.getDemandCount(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    public ServiceCategory toDomain(ServiceCategoryJpaEntity entity) {
        return ServiceCategory.reconstitute(
                CategoryId.of(entity.getId()),
                entity.getLabel(),
                IconKey.valueOf(entity.getIconKey().name()),
                entity.getColor(),
                entity.getDemandCount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
