package com.serviloc.notifications.domain.repository;

import java.util.List;

/**
 * Résultat paginé générique, indépendant de Spring Data.
 */
public record PageResult<T>(List<T> content, int page, int size, long totalElements) {

    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }
}
