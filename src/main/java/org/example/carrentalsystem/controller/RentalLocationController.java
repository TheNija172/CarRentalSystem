package org.example.carrentalsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carrentalsystem.dto.location.RentalLocationCreateRequest;
import org.example.carrentalsystem.dto.location.RentalLocationResponse;
import org.example.carrentalsystem.dto.location.RentalLocationUpdateRequest;
import org.example.carrentalsystem.service.RentalLocationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Rental Locations",
        description = "Operations for rental location management"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/rental-locations")
@RequiredArgsConstructor
public class RentalLocationController {

    private final RentalLocationService rentalLocationService;

    @Operation(
            summary = "Get rental location by ID",
            description = "Returns a rental location by its ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Location not found")
    })
    @GetMapping("/{id}")
    public RentalLocationResponse getById(@PathVariable Long id) {

        return rentalLocationService.getById(id);
    }

    @Operation(
            summary = "Get all rental locations",
            description = "Returns all rental locations"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Locations successfully retrieved"
    )
    @GetMapping
    public List<RentalLocationResponse> getAll() {

        return rentalLocationService.getAll();
    }

    @Operation(
            summary = "Create rental location",
            description = "Creates a new rental location. Available only to administrators"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Location successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid location data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RentalLocationResponse create(@Valid @RequestBody RentalLocationCreateRequest request) {

        return rentalLocationService.create(request);
    }

    @Operation(
            summary = "Update rental location",
            description = "Updates an existing rental location. Available only to administrators"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid location data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Location not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public RentalLocationResponse update(@PathVariable Long id, @Valid @RequestBody RentalLocationUpdateRequest request) {

        return rentalLocationService.update(id, request);
    }

    @Operation(
            summary = "Delete rental location",
            description = "Deletes a rental location. Available only to administrators"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Location successfully deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Location not found"),
            @ApiResponse(responseCode = "409", description = "Location is in use")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        rentalLocationService.delete(id);
    }
}