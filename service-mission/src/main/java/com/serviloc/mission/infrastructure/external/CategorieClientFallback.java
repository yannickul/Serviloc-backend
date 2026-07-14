package com.serviloc.mission.infrastructure.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CategorieClientFallback implements CategorieClient {

    @Override
    public CategorySummary getCategoryById(String id) {
        log.warn("CategorieClient indisponible — categoryLabel non résolu pour categoryId={}", id);
        CategorySummary fallback = new CategorySummary();
        fallback.setId(id);
        fallback.setLabel(null);
        fallback.setIconKey(null);
        return fallback;
    }
}