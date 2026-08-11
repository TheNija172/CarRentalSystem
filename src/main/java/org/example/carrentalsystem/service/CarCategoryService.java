package org.example.carrentalsystem.service;

import org.example.carrentalsystem.dto.category.CarCategoryCreateRequest;
import org.example.carrentalsystem.dto.category.CarCategoryResponse;
import org.example.carrentalsystem.dto.category.CarCategoryUpdateRequest;

import java.util.List;

public interface CarCategoryService {

    CarCategoryResponse create(CarCategoryCreateRequest request);

    CarCategoryResponse getById(Long id);

    List<CarCategoryResponse> getAll();

    CarCategoryResponse update(Long id, CarCategoryUpdateRequest request);

    void delete(Long id);
}
