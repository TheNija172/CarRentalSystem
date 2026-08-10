package org.example.carrentalsystem.controller;

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

@RestController
@RequestMapping("/api/v1/rental-locations")
@RequiredArgsConstructor
public class RentalLocationController {

    private final RentalLocationService rentalLocationService;

    @GetMapping("/{id}")
    public RentalLocationResponse getById(@PathVariable Long id) {

        return rentalLocationService.getById(id);
    }

    @GetMapping
    public List<RentalLocationResponse> getAll() {

        return rentalLocationService.getAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RentalLocationResponse create(@Valid @RequestBody RentalLocationCreateRequest request) {

        return rentalLocationService.create(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public RentalLocationResponse update(@PathVariable Long id, @Valid @RequestBody RentalLocationUpdateRequest request) {

        return rentalLocationService.update(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        rentalLocationService.delete(id);
    }
}