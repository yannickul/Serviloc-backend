package com.serviloc.notifications.domain.repository;

/**
 * Requête de pagination simple, indépendante de Spring Data, pour préserver la pureté du domaine.
 */
public record PageQuery(int page, int size) {

    public PageQuery {
        if (page < 0) {
            throw new IllegalArgumentException("page doit être >= 0");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("size doit être entre 1 et 100");
        }
    }

    public static PageQuery of(int page, int size) {
        return new PageQuery(page, size);
    }
}
