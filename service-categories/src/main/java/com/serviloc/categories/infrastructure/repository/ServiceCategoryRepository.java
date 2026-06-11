package com.serviloc.categories.infrastructure.repository;

import com.serviloc.categories.domain.model.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {
    Optional<Object> findBySlug(String slug);
    // Tu peux ajouter des méthodes custom si besoin, ex:
    // Optional<ServiceCategory> findBySlug(String slug);

    public interface CategoryRepository {
        List<ServiceCategory> findAll();
        Optional<ServiceCategory> findBySlug(String slug); // ajouté
        ServiceCategory save(ServiceCategory category);
        void delete(Long id);
    }

}
