package com.serviloc.categories.application.dto;

import com.serviloc.categories.domain.model.ServiceCategory;

/**
 * DTO utilisé pour la création ou la mise à jour d'une catégorie.
 * Ne contient pas l'id interne, mais permet de générer un slug.
 */
public record CategoryDto(
        String label,
        String iconKey,
        String color
) {
    /** Conversion DTO → Domaine (slug généré automatiquement) */
    public ServiceCategory toDomain() {
        return ServiceCategory.create(
                label,
                iconKey,
                color
        );
    }

    /** Conversion DTO → Domaine avec slug fourni (utile pour update) */
    public ServiceCategory toDomainWithSlug(String slug) {
        return ServiceCategory.reconstitute(
                null,       // id interne non exposé
                slug,       // identifiant public
                label,
                iconKey,
                color,
                0,
                0.0
        );
    }
}
