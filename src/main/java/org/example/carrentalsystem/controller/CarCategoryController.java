package org.example.carrentalsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carrentalsystem.dto.category.CarCategoryCreateRequest;
import org.example.carrentalsystem.dto.category.CarCategoryResponse;
import org.example.carrentalsystem.dto.category.CarCategoryUpdateRequest;
import org.example.carrentalsystem.service.CarCategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/car-categories")
@RequiredArgsConstructor
public class CarCategoryController {

    private final CarCategoryService carCategoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarCategoryResponse create(@Valid @RequestBody CarCategoryCreateRequest request) {

        return carCategoryService.create(request);
    }

    @GetMapping("/{id}")
    public CarCategoryResponse getById(@PathVariable Long id) {

        return carCategoryService.getById(id);
    }

    @GetMapping
    public List<CarCategoryResponse> getAll() {

        return carCategoryService.getAll();
    }

    @PutMapping("/{id}")
    public CarCategoryResponse update(@PathVariable Long id, @Valid @RequestBody CarCategoryUpdateRequest request) {

        return carCategoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        carCategoryService.delete(id);
    }
}
