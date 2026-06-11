package com.serviloc.categories.domain.exception;

/**
 * Exception lancée lorsqu'une catégorie n'est pas trouvée.
 */
public class CategoryNotFoundException extends RuntimeException {

    /** Constructeur pour slug (identifiant public exposé côté API) */
    public CategoryNotFoundException(String slug) {
        super("Category not found with slug: " + slug);
    }

    /** Constructeur pour id interne (clé primaire DB) */
    public CategoryNotFoundException(Long id) {
        super("Category not found with internal id: " + id);
    }
}
