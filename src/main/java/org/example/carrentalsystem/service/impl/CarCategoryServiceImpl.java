package org.example.carrentalsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carrentalsystem.dto.category.CarCategoryCreateRequest;
import org.example.carrentalsystem.dto.category.CarCategoryResponse;
import org.example.carrentalsystem.dto.category.CarCategoryUpdateRequest;
import org.example.carrentalsystem.entity.CarCategory;
import org.example.carrentalsystem.exception.CategoryInUseException;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.mapper.CarCategoryMapper;
import org.example.carrentalsystem.repository.CarCategoryRepository;
import org.example.carrentalsystem.repository.CarRepository;
import org.example.carrentalsystem.service.CarCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarCategoryServiceImpl implements CarCategoryService {

    private final CarCategoryRepository carCategoryRepository;
    private final CarRepository carRepository;
    private final CarCategoryMapper carCategoryMapper;


    @Override
    @Transactional
    public CarCategoryResponse create(CarCategoryCreateRequest request) {

        CarCategory category = carCategoryMapper.toEntity(request);

        CarCategory saveCategory = carCategoryRepository.save(category);

        return carCategoryMapper.toResponse(saveCategory);
    }

    @Override
    public CarCategoryResponse getById(Long id) {

        CarCategory category = carCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Car category with id " + id + " not found"
                ));

        return carCategoryMapper.toResponse(category);
    }

    @Override
    public List<CarCategoryResponse> getAll() {

        return carCategoryRepository.findAll()
                .stream()
                .map(carCategoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CarCategoryResponse update(Long id, CarCategoryUpdateRequest request) {

        CarCategory category = carCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Car category with id " + id + " not found"
                ));

        carCategoryMapper.updateEntity(request, category);

        CarCategory updatedCategory = carCategoryRepository.save(category);

        return carCategoryMapper.toResponse(updatedCategory);
    }

    @Override
    @Transactional
    public void delete(Long id) {

        CarCategory category = carCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Car category with id " + id + " not found"
                ));

        if (carRepository.existsByCategoryId(id)) {
            throw new CategoryInUseException(
                    "Cannot delete category because it is used by cars"
            );
        }

        carCategoryRepository.delete(category);

    }
}
