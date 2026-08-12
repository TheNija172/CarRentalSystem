package org.example.carrentalsystem.service;

import org.example.carrentalsystem.dto.category.CarCategoryCreateRequest;
import org.example.carrentalsystem.dto.category.CarCategoryResponse;
import org.example.carrentalsystem.dto.category.CarCategoryUpdateRequest;
import org.example.carrentalsystem.entity.CarCategoryEntity;
import org.example.carrentalsystem.exception.CategoryInUseException;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.mapper.CarCategoryMapper;
import org.example.carrentalsystem.repository.CarCategoryRepository;
import org.example.carrentalsystem.repository.CarRepository;
import org.example.carrentalsystem.service.impl.CarCategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarCategoryServiceImplTest {

    @Mock
    private CarCategoryRepository carCategoryRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarCategoryMapper carCategoryMapper;

    @InjectMocks
    private CarCategoryServiceImpl carCategoryService;

    private CarCategoryEntity categoryEntity;
    private CarCategoryCreateRequest createRequest;
    private CarCategoryUpdateRequest updateRequest;
    private CarCategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        categoryEntity = new CarCategoryEntity();
        categoryEntity.setId(1L);
        categoryEntity.setName("Economy");
        categoryEntity.setDescription("Economy class cars");
        categoryEntity.setPricePerDay(new BigDecimal("50.00"));

        createRequest = new CarCategoryCreateRequest();
        createRequest.setName("Economy");
        createRequest.setDescription("Economy class cars");
        createRequest.setPricePerDay(new BigDecimal("50.00"));

        updateRequest = new CarCategoryUpdateRequest();
        updateRequest.setName("Economy Updated");
        updateRequest.setDescription("Updated description");
        updateRequest.setPricePerDay(new BigDecimal("60.00"));

        categoryResponse = new CarCategoryResponse();
        categoryResponse.setId(1L);
        categoryResponse.setName("Economy");
        categoryResponse.setDescription("Economy class cars");
        categoryResponse.setPricePerDay(new BigDecimal("50.00"));
        categoryResponse.setCreatedAt(LocalDateTime.now());
        categoryResponse.setUpdatedAt(LocalDateTime.now());
    }

    //----------Create----------

    @Test
    void create_shouldCreateCategorySuccessfully() {
        when(carCategoryMapper.toEntity(createRequest)).thenReturn(categoryEntity);
        when(carCategoryRepository.save(categoryEntity)).thenReturn(categoryEntity);
        when(carCategoryMapper.toResponse(categoryEntity)).thenReturn(categoryResponse);

        CarCategoryResponse result = carCategoryService.create(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(categoryEntity.getName());
        verify(carCategoryMapper).toEntity(createRequest);
        verify(carCategoryRepository).save(categoryEntity);
        verify(carCategoryMapper).toResponse(categoryEntity);
    }

    @Test
    void create_shouldThrowException_whenCategoryAlreadyExists() {
        when(carCategoryMapper.toEntity(createRequest)).thenReturn(categoryEntity);
        when(carCategoryRepository.save(any(CarCategoryEntity.class)))
                .thenThrow(new DataIntegrityViolationException("Category already exists"));

        assertThatThrownBy(() -> carCategoryService.create(createRequest))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(carCategoryRepository).save(any(CarCategoryEntity.class));
    }

    //----------GetById----------

    @Test
    void getById_shouldReturnCategory() {
        Long categoryId = 1L;
        when(carCategoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryEntity));
        when(carCategoryMapper.toResponse(categoryEntity)).thenReturn(categoryResponse);

        CarCategoryResponse result = carCategoryService.getById(categoryId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
    }

    @Test
    void getById_shouldThrowException_whenCategoryNotFound() {
        Long categoryId = 99L;
        when(carCategoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carCategoryService.getById(categoryId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    //----------GetAll----------

    @Test
    void getAll_shouldReturnCategories() {
        when(carCategoryRepository.findAll()).thenReturn(List.of(categoryEntity));
        when(carCategoryMapper.toResponse(categoryEntity)).thenReturn(categoryResponse);

        List<CarCategoryResponse> result = carCategoryService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(carCategoryRepository).findAll();
    }

    //----------Update----------

    @Test
    void update_shouldUpdateCategorySuccessfully() {
        Long categoryId = 1L;
        when(carCategoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryEntity));
        doNothing().when(carCategoryMapper).updateEntity(updateRequest, categoryEntity);
        when(carCategoryRepository.save(categoryEntity)).thenReturn(categoryEntity);
        when(carCategoryMapper.toResponse(categoryEntity)).thenReturn(categoryResponse);

        CarCategoryResponse result = carCategoryService.update(categoryId, updateRequest);

        assertThat(result).isNotNull();
        verify(carCategoryMapper).updateEntity(updateRequest, categoryEntity);
        verify(carCategoryRepository).save(categoryEntity);
    }

    @Test
    void update_shouldThrowException_whenCategoryNotFound() {
        Long categoryId = 99L;
        when(carCategoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carCategoryService.update(categoryId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    //----------Delete----------

    @Test
    void delete_shouldDeleteCategorySuccessfully() {
        Long categoryId = 1L;
        when(carCategoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryEntity));
        when(carRepository.existsByCategoryId(categoryId)).thenReturn(false);
        doNothing().when(carCategoryRepository).delete(categoryEntity);

        carCategoryService.delete(categoryId);

        verify(carCategoryRepository).delete(categoryEntity);
    }

    @Test
    void delete_shouldThrowException_whenCategoryNotFound() {
        Long categoryId = 99L;
        when(carCategoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carCategoryService.delete(categoryId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");

        verify(carCategoryRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowException_whenCategoryIsInUse() {
        Long categoryId = 1L;
        when(carCategoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryEntity));
        when(carRepository.existsByCategoryId(categoryId)).thenReturn(true);

        assertThatThrownBy(() -> carCategoryService.delete(categoryId))
                .isInstanceOf(CategoryInUseException.class)
                .hasMessageContaining("used by cars");

        verify(carCategoryRepository, never()).delete(any());
    }
}
