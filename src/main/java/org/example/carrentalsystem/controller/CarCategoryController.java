package org.example.carrentalsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carrentalsystem.dto.category.CarCategoryCreateRequest;
import org.example.carrentalsystem.dto.category.CarCategoryResponse;
import org.example.carrentalsystem.dto.category.CarCategoryUpdateRequest;
import org.example.carrentalsystem.service.CarCategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Car Categories",
        description = "Operations for car category management"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/car-categories")
@RequiredArgsConstructor
public class CarCategoryController {

    private final CarCategoryService carCategoryService;

    @Operation(
            summary = "Get car category by ID",
            description = "Returns a car category by its ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{id}")
    public CarCategoryResponse getById(@PathVariable Long id) {

        return carCategoryService.getById(id);
    }

    @Operation(
            summary = "Get all car categories",
            description = "Returns all car categories"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Categories successfully retrieved"
    )
    @GetMapping
    public List<CarCategoryResponse> getAll() {

        return carCategoryService.getAll();
    }

    @Operation(
            summary = "Create car category",
            description = "Creates a new car category. Available only to administrators"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid category data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarCategoryResponse create(@Valid @RequestBody CarCategoryCreateRequest request) {

        return carCategoryService.create(request);
    }

    @Operation(
            summary = "Update car category",
            description = "Updates an existing car category. Available only to administrators"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid category data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public CarCategoryResponse update(@PathVariable Long id, @Valid @RequestBody CarCategoryUpdateRequest request) {

        return carCategoryService.update(id, request);
    }

    @Operation(
            summary = "Delete car category",
            description = "Deletes a car category. Available only to administrators"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category successfully deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Category is in use by cars")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        carCategoryService.delete(id);
    }
}
