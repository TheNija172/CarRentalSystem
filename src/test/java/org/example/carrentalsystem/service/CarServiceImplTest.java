package org.example.carrentalsystem.service;

import org.example.carrentalsystem.dto.car.CarCreateRequest;
import org.example.carrentalsystem.dto.car.CarResponse;
import org.example.carrentalsystem.dto.car.CarUpdateRequest;
import org.example.carrentalsystem.entity.CarCategoryEntity;
import org.example.carrentalsystem.entity.CarEntity;
import org.example.carrentalsystem.enums.FuelType;
import org.example.carrentalsystem.enums.Transmission;
import org.example.carrentalsystem.exception.CarAlreadyExistsException;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.mapper.CarMapper;
import org.example.carrentalsystem.repository.CarCategoryRepository;
import org.example.carrentalsystem.repository.CarRepository;
import org.example.carrentalsystem.service.impl.CarServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceImplTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarCategoryRepository carCategoryRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarServiceImpl carService;

    private CarEntity carEntity;
    private CarCategoryEntity categoryEntity;
    private CarCreateRequest createRequest;
    private CarUpdateRequest updateRequest;
    private CarResponse carResponse;

    @BeforeEach
    void setUp() {
        categoryEntity = new CarCategoryEntity();
        categoryEntity.setId(1L);
        categoryEntity.setName("Economy");
        categoryEntity.setPricePerDay(new BigDecimal("50.00"));

        carEntity = new CarEntity();
        carEntity.setId(1L);
        carEntity.setBrand("Toyota");
        carEntity.setModel("Camry");
        carEntity.setProductionYear(2022);
        carEntity.setLicensePlate("A123BC");
        carEntity.setColor("Black");
        carEntity.setTransmission(Transmission.AUTOMATIC);
        carEntity.setFuelType(FuelType.PETROL);
        carEntity.setSeats(5);
        carEntity.setActive(true);
        carEntity.setCategory(categoryEntity);

        createRequest = new CarCreateRequest();
        createRequest.setBrand("Toyota");
        createRequest.setModel("Camry");
        createRequest.setProductionYear(2022);
        createRequest.setLicensePlate("A123BC");
        createRequest.setColor("Black");
        createRequest.setTransmission(Transmission.AUTOMATIC);
        createRequest.setFuelType(FuelType.PETROL);
        createRequest.setSeats(5);
        createRequest.setCategoryId(1L);

        updateRequest = new CarUpdateRequest();
        updateRequest.setBrand("Toyota");
        updateRequest.setModel("Camry");
        updateRequest.setProductionYear(2022);
        updateRequest.setLicensePlate("A123BC");
        updateRequest.setColor("Black");
        updateRequest.setTransmission(Transmission.AUTOMATIC);
        updateRequest.setFuelType(FuelType.PETROL);
        updateRequest.setSeats(5);
        updateRequest.setActive(true);
        updateRequest.setCategoryId(1L);

        carResponse = new CarResponse();
        carResponse.setId(1L);
        carResponse.setBrand("Toyota");
        carResponse.setModel("Camry");
    }

    //----------Create----------

    @Test
    void create_shouldCreateCarSuccessfully() {

        when(carRepository.existsByLicensePlate(createRequest.getLicensePlate())).thenReturn(false);
        when(carCategoryRepository.findById(createRequest.getCategoryId())).thenReturn(Optional.of(categoryEntity));
        when(carMapper.toEntity(createRequest)).thenReturn(carEntity);
        when(carRepository.save(carEntity)).thenReturn(carEntity);
        when(carMapper.toResponse(carEntity)).thenReturn(carResponse);

        CarResponse result = carService.create(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getBrand()).isEqualTo(carEntity.getBrand());
        verify(carRepository).existsByLicensePlate(createRequest.getLicensePlate());
        verify(carCategoryRepository).findById(createRequest.getCategoryId());
        verify(carMapper).toEntity(createRequest);
        verify(carRepository).save(carEntity);
    }

    @Test
    void create_shouldThrowException_whenLicensePlateAlreadyExists() {

        when(carRepository.existsByLicensePlate(createRequest.getLicensePlate())).thenReturn(true);

        assertThatThrownBy(() -> carService.create(createRequest))
                .isInstanceOf(CarAlreadyExistsException.class)
                .hasMessageContaining("already exists");

        verify(carRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowException_whenCategoryNotFound() {

        when(carRepository.existsByLicensePlate(createRequest.getLicensePlate())).thenReturn(false);
        when(carCategoryRepository.findById(createRequest.getCategoryId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");

        verify(carRepository, never()).save(any());
    }

    //----------GetById----------
    @Test
    void getById_shouldReturnCar() {

        Long carId = 1L;
        when(carRepository.findByIdAndActiveTrue(carId)).thenReturn(Optional.of(carEntity));
        when(carMapper.toResponse(carEntity)).thenReturn(carResponse);

        CarResponse result = carService.getById(carId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(carId);
    }

    @Test
    void getById_shouldThrowException_whenCarNotFound() {

        Long carId = 99L;
        when(carRepository.findByIdAndActiveTrue(carId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.getById(carId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }


    //----------GetAll----------
    @Test
    void getAll_shouldReturnCars() {

        when(carRepository.findAllByActiveTrue()).thenReturn(List.of(carEntity));
        when(carMapper.toResponse(carEntity)).thenReturn(carResponse);

        List<CarResponse> result = carService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(carRepository).findAllByActiveTrue();
    }

    //----------Update----------
    @Test
    void update_shouldUpdateCarSuccessfully() {

        Long carId = 1L;
        when(carRepository.findById(carId)).thenReturn(Optional.of(carEntity));
        when(carRepository.existsByLicensePlateAndIdNot(updateRequest.getLicensePlate(), carId)).thenReturn(false);
        when(carCategoryRepository.findById(updateRequest.getCategoryId())).thenReturn(Optional.of(categoryEntity));

        doNothing().when(carMapper).updateEntity(updateRequest, carEntity);

        when(carRepository.save(carEntity)).thenReturn(carEntity);
        when(carMapper.toResponse(carEntity)).thenReturn(carResponse);

        CarResponse result = carService.update(carId, updateRequest);

        assertThat(result).isNotNull();
        verify(carMapper).updateEntity(updateRequest, carEntity);
        verify(carRepository).save(carEntity);
    }

    @Test
    void update_shouldThrowException_whenCarNotFound() {

        Long carId = 99L;
        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.update(carId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void update_shouldThrowException_whenLicensePlateAlreadyExists() {

        Long carId = 1L;
        when(carRepository.findById(carId)).thenReturn(Optional.of(carEntity));
        when(carRepository.existsByLicensePlateAndIdNot(updateRequest.getLicensePlate(), carId)).thenReturn(true);

        assertThatThrownBy(() -> carService.update(carId, updateRequest))
                .isInstanceOf(CarAlreadyExistsException.class)
                .hasMessageContaining("already exists");

        verify(carRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowException_whenCategoryNotFound() {

        Long carId = 1L;
        when(carRepository.findById(carId)).thenReturn(Optional.of(carEntity));
        when(carRepository.existsByLicensePlateAndIdNot(updateRequest.getLicensePlate(), carId)).thenReturn(false);
        when(carCategoryRepository.findById(updateRequest.getCategoryId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.update(carId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");

        verify(carRepository, never()).save(any());
    }


    //----------Delete----------
    @Test
    void delete_shouldDeactivateCarSuccessfully() {

        Long carId = 1L;
        when(carRepository.findById(carId)).thenReturn(Optional.of(carEntity));
        when(carRepository.save(carEntity)).thenReturn(carEntity);

        carService.delete(carId);

        assertThat(carEntity.isActive()).isFalse();
        verify(carRepository).save(carEntity);
    }

    @Test
    void delete_shouldThrowException_whenCarNotFound() {

        Long carId = 99L;
        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.delete(carId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");

        verify(carRepository, never()).save(any());
    }
}
