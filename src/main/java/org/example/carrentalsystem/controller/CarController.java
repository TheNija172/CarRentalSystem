package org.example.carrentalsystem.controller;

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

@RestController
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @GetMapping("/{id}")
    public CarResponse getById(@PathVariable Long id) {

        return carService.getById(id);
    }

    @GetMapping
    public List<CarResponse> getAll() {

        return carService.getAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarResponse create(@Valid @RequestBody CarCreateRequest request) {

        return carService.create(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public CarResponse update(@PathVariable Long id, @Valid @RequestBody CarUpdateRequest request) {

        return carService.update(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        carService.delete(id);
    }
}
