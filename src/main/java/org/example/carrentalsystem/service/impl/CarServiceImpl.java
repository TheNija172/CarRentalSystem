package org.example.carrentalsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carrentalsystem.dto.car.CarCreateRequest;
import org.example.carrentalsystem.dto.car.CarResponse;
import org.example.carrentalsystem.dto.car.CarUpdateRequest;
import org.example.carrentalsystem.entity.Car;
import org.example.carrentalsystem.entity.CarCategory;
import org.example.carrentalsystem.exception.CarAlreadyExistsException;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.mapper.CarMapper;
import org.example.carrentalsystem.repository.CarCategoryRepository;
import org.example.carrentalsystem.repository.CarRepository;
import org.example.carrentalsystem.service.CarService;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final CarMapper carMapper;
    private final CarCategoryRepository carCategoryRepository;

    @Override
    public CarResponse create(CarCreateRequest request) {

        if (carRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new CarAlreadyExistsException(
                    "Car with license plate "
                            + request.getLicensePlate()
                            + " already exists"
            );
        }

        CarCategory category = carCategoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Car categori with id "
                                + request.getCategoryId()
                                + " not found"
                ));

        Car car = carMapper.toEntity(request);

        car.setCategory(category);

        Car savedCar = carRepository.save(car);

        return carMapper.toResponse(savedCar);
    }

    @Override
    public CarResponse getById(Long id) {

        Car car = carRepository.findByIdAndActiveTrue(id).orElseThrow(() ->
                new ResourceNotFoundException("Car with id " + id + " not found"));

        return carMapper.toResponse(car);
    }

    @Override
    public List<CarResponse> getAll() {

        return carRepository.findAllByActiveTrue()
                .stream()
                .map(carMapper::toResponse)
                .toList();
    }

    @Override
    public CarResponse update(Long id, CarUpdateRequest request) {

        Car car = carRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Car with id " + id + " not found"));

        if(carRepository.existsByLicensePlateAndIdNot(request.getLicensePlate(), id)) {

            throw new CarAlreadyExistsException(
                    "Car with license plate "
                    + request.getLicensePlate()
                    + " already exists"
            );
        }

        CarCategory category = carCategoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category with id " + request.getCategoryId() + " not found"
                ));

        car.setCategory(category);

        carMapper.updateEntity(request, car);

        Car updatedCar = carRepository.save(car);

        return carMapper.toResponse(updatedCar);
    }

    @Override
    public void delete(Long id) {

        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Car with id " + id + " not found"
                ));

        car.setActive(false);

        carRepository.save(car);
    }
}
