package com.serviloc.categories.adapter.rest;

import com.serviloc.categories.application.service.CategoryService;
import com.serviloc.categories.domain.model.ServiceCategory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/client/categories")
public class ClientCategoryController {

    private final CategoryService service;

    public ClientCategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<ServiceCategory> getAll() {
        return service.getAllForClient();
    }
}
