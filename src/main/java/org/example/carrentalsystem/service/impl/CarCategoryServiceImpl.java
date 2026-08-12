package org.example.carrentalsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalsystem.dto.category.CarCategoryCreateRequest;
import org.example.carrentalsystem.dto.category.CarCategoryResponse;
import org.example.carrentalsystem.dto.category.CarCategoryUpdateRequest;
import org.example.carrentalsystem.entity.CarCategoryEntity;
import org.example.carrentalsystem.exception.CategoryInUseException;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.mapper.CarCategoryMapper;
import org.example.carrentalsystem.repository.CarCategoryRepository;
import org.example.carrentalsystem.repository.CarRepository;
import org.example.carrentalsystem.service.CarCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
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

        log.info("Creating car category: name={}", request.getName());

        CarCategoryEntity category = carCategoryMapper.toEntity(request);

        CarCategoryEntity savedCategory = carCategoryRepository.save(category);

        log.info(
                "Car category created successfully: categoryId={}",
                savedCategory.getId()
        );

        return carCategoryMapper.toResponse(savedCategory);
    }

    @Override
    public CarCategoryResponse getById(Long id) {

        CarCategoryEntity category = carCategoryRepository.findById(id)
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

        CarCategoryEntity category = carCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Car category with id " + id + " not found"
                ));

        carCategoryMapper.updateEntity(request, category);

        CarCategoryEntity updatedCategory = carCategoryRepository.save(category);

        return carCategoryMapper.toResponse(updatedCategory);
    }

    @Override
    @Transactional
    public void delete(Long id) {

        log.info("Deleting car category: id={}", id);

        CarCategoryEntity category = carCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Car category with id " + id + " not found"
                ));

        if (carRepository.existsByCategoryId(id)) {
            log.warn("Cannot delete category because it is used by cars");

            throw new CategoryInUseException(
                    "Cannot delete category because it is used by cars"
            );
        }

        log.info("Creating car category with id {} deleted", id);

        carCategoryRepository.delete(category);

    }
}
