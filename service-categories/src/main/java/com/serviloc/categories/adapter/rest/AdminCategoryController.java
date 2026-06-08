package com.serviloc.categories.adapter.rest;

import com.serviloc.categories.application.dto.CategoryDto;
import com.serviloc.categories.application.service.CategoryService;
import com.serviloc.categories.domain.model.ServiceCategory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService service;

    public AdminCategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<ServiceCategory> getAll() {
        return service.getAllForAdmin();
    }

    @PostMapping
    public ServiceCategory create(@RequestBody CategoryDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public ServiceCategory update(@PathVariable Long id, @RequestBody CategoryDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
