package org.example.carrentalsystem.service;

import org.example.carrentalsystem.dto.car.CarCreateRequest;
import org.example.carrentalsystem.dto.car.CarResponse;
import org.example.carrentalsystem.dto.car.CarUpdateRequest;

import java.util.List;

public interface CarService {

    CarResponse create(CarCreateRequest request);

    CarResponse getById(Long id);

    List<CarResponse> getAll();

    CarResponse update(Long id, CarUpdateRequest request);

    void delete(Long id);
}
