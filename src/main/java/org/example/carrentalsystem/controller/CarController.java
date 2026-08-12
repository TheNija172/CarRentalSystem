package org.example.carrentalsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carrentalsystem.dto.car.CarCreateRequest;
import org.example.carrentalsystem.dto.car.CarResponse;
import org.example.carrentalsystem.dto.car.CarUpdateRequest;
import org.example.carrentalsystem.service.CarService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Cars",
        description = "Operations for car management"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @Operation(
            summary = "Get car by ID",
            description = "Returns an active car by its ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Car successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Car not found")
    })
    @GetMapping("/{id}")
    public CarResponse getById(@PathVariable Long id) {

        return carService.getById(id);
    }

    @Operation(summary = "Get all active cars", description = "Returns all active cars")
    @ApiResponse(responseCode = "200", description = "Cars successfully retrieved")
    @GetMapping
    public List<CarResponse> getAll() {

        return carService.getAll();
    }

    @Operation(
            summary = "Create car",
            description = "Creates a new car. Available only to administrators"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Car successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid car data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "409", description = "Car with this license plate already exists")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarResponse create(@Valid @RequestBody CarCreateRequest request) {

        return carService.create(request);
    }

    @Operation(
            summary = "Update car",
            description = "Updates an existing car. Available only to administrators"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Car successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid car data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Car not found"),
            @ApiResponse(responseCode = "409", description = "Car with this license plate already exists")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public CarResponse update(@PathVariable Long id, @Valid @RequestBody CarUpdateRequest request) {

        return carService.update(id, request);
    }

    @Operation(
            summary = "Delete car",
            description = "Deletes a car. Available only to administrators"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Car successfully deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Car not found"),
            @ApiResponse(responseCode = "409", description = "Car cannot be deleted because it is in use")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        carService.delete(id);
    }
}
