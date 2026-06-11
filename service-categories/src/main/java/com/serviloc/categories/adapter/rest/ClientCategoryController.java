package com.serviloc.categories.adapter.rest;

import com.serviloc.categories.application.dto.CategoryResponseDto;
import com.serviloc.categories.application.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/client/categories")
public class ClientCategoryController {

    private final CategoryService service;

    public ClientCategoryController(CategoryService service) {
        this.service = service;
    }

    /** Liste des catégories côté client (sans stats) */
    @GetMapping
    public List<CategoryResponseDto> listCategories() {
        return service.getAllForClient();
    }
}
