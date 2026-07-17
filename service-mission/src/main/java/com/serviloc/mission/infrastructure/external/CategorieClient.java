// infrastructure/external/CategorieClient.java
package com.serviloc.mission.infrastructure.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "service-categories",
        fallback = CategorieClientFallback.class
)
public interface CategorieClient {

    @GetMapping("/internal/categories/{id}")
    CategorySummary getCategoryById(@PathVariable String id);
}