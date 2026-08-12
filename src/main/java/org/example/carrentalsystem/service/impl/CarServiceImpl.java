package org.example.carrentalsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalsystem.dto.car.CarCreateRequest;
import org.example.carrentalsystem.dto.car.CarResponse;
import org.example.carrentalsystem.dto.car.CarUpdateRequest;
import org.example.carrentalsystem.entity.CarCategoryEntity;
import org.example.carrentalsystem.entity.CarEntity;
import org.example.carrentalsystem.exception.CarAlreadyExistsException;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.mapper.CarMapper;
import org.example.carrentalsystem.repository.CarCategoryRepository;
import org.example.carrentalsystem.repository.CarRepository;
import org.example.carrentalsystem.service.CarService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final CarMapper carMapper;
    private final CarCategoryRepository carCategoryRepository;

    @Override
    @Transactional
    public CarResponse create(CarCreateRequest request) {

        log.info("Creating car: band={}, model={}",
                request.getBrand(),
                request.getModel());

        if (carRepository.existsByLicensePlate(request.getLicensePlate())) {

            log.warn("Car with license plate {} already exists", request.getLicensePlate());

            throw new CarAlreadyExistsException(
                    "Car with license plate "
                            + request.getLicensePlate()
                            + " already exists"
            );
        }

        CarCategoryEntity category = carCategoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Car categori with id "
                                + request.getCategoryId()
                                + " not found"
                ));

        CarEntity carEntity = carMapper.toEntity(request);

        carEntity.setCategory(category);

        CarEntity savedCar = carRepository.save(carEntity);

        log.info("Car created successfully: brand={}, model={}", savedCar.getBrand(), savedCar.getModel());

        return carMapper.toResponse(savedCar);
    }

    @Override
    public CarResponse getById(Long id) {

        CarEntity carEntity = carRepository.findByIdAndActiveTrue(id).orElseThrow(() ->
                new ResourceNotFoundException("Car with id " + id + " not found"));

        return carMapper.toResponse(carEntity);
    }

    @Override
    public List<CarResponse> getAll() {

        return carRepository.findAllByActiveTrue()
                .stream()
                .map(carMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CarResponse update(Long id, CarUpdateRequest request) {

        CarEntity carEntity = carRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Car with id " + id + " not found"));

        if (carRepository.existsByLicensePlateAndIdNot(request.getLicensePlate(), id)) {

            throw new CarAlreadyExistsException(
                    "Car with license plate "
                            + request.getLicensePlate()
                            + " already exists"
            );
        }

        CarCategoryEntity category = carCategoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category with id " + request.getCategoryId() + " not found"
                ));

        carEntity.setCategory(category);

        carMapper.updateEntity(request, carEntity);

        CarEntity updatedCarEntity = carRepository.save(carEntity);

        return carMapper.toResponse(updatedCarEntity);
    }

    @Override
    @Transactional
    public void delete(Long id) {

        log.info("Deleting car: id={}", id);

        CarEntity carEntity = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Car with id " + id + " not found"
                ));

        carEntity.setActive(false);

        carRepository.save(carEntity);

        log.info("Car deleted: id={}", id);
    }
}
