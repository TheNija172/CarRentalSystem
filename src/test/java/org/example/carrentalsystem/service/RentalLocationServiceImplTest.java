package org.example.carrentalsystem.service;

import org.example.carrentalsystem.dto.location.RentalLocationCreateRequest;
import org.example.carrentalsystem.dto.location.RentalLocationResponse;
import org.example.carrentalsystem.dto.location.RentalLocationUpdateRequest;
import org.example.carrentalsystem.entity.RentalLocationEntity;
import org.example.carrentalsystem.exception.BusinessException;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.mapper.RentalLocationMapper;
import org.example.carrentalsystem.repository.RentalLocationRepository;
import org.example.carrentalsystem.service.impl.RentalLocationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalLocationServiceImplTest {

    @Mock
    private RentalLocationRepository rentalLocationRepository;

    @Mock
    private RentalLocationMapper rentalLocationMapper;

    @InjectMocks
    private RentalLocationServiceImpl rentalLocationService;

    private RentalLocationEntity locationEntity;
    private RentalLocationCreateRequest createRequest;
    private RentalLocationUpdateRequest updateRequest;
    private RentalLocationResponse locationResponse;

    @BeforeEach
    void setUp() {
        locationEntity = new RentalLocationEntity();
        locationEntity.setId(1L);
        locationEntity.setName("Downtown Office");
        locationEntity.setAddress("123 Main St");
        locationEntity.setCity("New York");
        locationEntity.setDescription("Central location");
        locationEntity.setActive(true);
        locationEntity.setCreatedAt(LocalDateTime.now());
        locationEntity.setUpdatedAt(LocalDateTime.now());

        createRequest = new RentalLocationCreateRequest();
        createRequest.setName("Downtown Office");
        createRequest.setAddress("123 Main St");
        createRequest.setCity("New York");
        createRequest.setDescription("Central location");

        updateRequest = new RentalLocationUpdateRequest();
        updateRequest.setName("Downtown Office Updated");
        updateRequest.setAddress("123 Main St");
        updateRequest.setCity("New York");
        updateRequest.setDescription("Updated description");
        updateRequest.setActive(true);

        locationResponse = new RentalLocationResponse();
        locationResponse.setId(1L);
        locationResponse.setName("Downtown Office");
        locationResponse.setAddress("123 Main St");
        locationResponse.setCity("New York");
        locationResponse.setDescription("Central location");
        locationResponse.setActive(true);
        locationResponse.setCreatedAt(LocalDateTime.now());
        locationResponse.setUpdatedAt(LocalDateTime.now());
    }

    //----------Create----------

    @Test
    void create_shouldCreateLocationSuccessfully() {
        when(rentalLocationRepository.existsByNameAndAddress(createRequest.getName(), createRequest.getAddress())).thenReturn(false);
        when(rentalLocationMapper.toEntity(createRequest)).thenReturn(locationEntity);
        when(rentalLocationRepository.save(locationEntity)).thenReturn(locationEntity);
        when(rentalLocationMapper.toResponse(locationEntity)).thenReturn(locationResponse);

        RentalLocationResponse result = rentalLocationService.create(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(locationEntity.getName());
        verify(rentalLocationRepository).existsByNameAndAddress(createRequest.getName(), createRequest.getAddress());
        verify(rentalLocationMapper).toEntity(createRequest);
        verify(rentalLocationRepository).save(locationEntity);
        verify(rentalLocationMapper).toResponse(locationEntity);
    }

    @Test
    void create_shouldThrowException_whenLocationAlreadyExists() {
        when(rentalLocationRepository.existsByNameAndAddress(createRequest.getName(), createRequest.getAddress())).thenReturn(true);

        assertThatThrownBy(() -> rentalLocationService.create(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");

        verify(rentalLocationRepository, never()).save(any());
    }

    //----------GetById----------

    @Test
    void getById_shouldReturnLocation() {
        Long locationId = 1L;
        when(rentalLocationRepository.findByIdAndActiveTrue(locationId)).thenReturn(Optional.of(locationEntity));
        when(rentalLocationMapper.toResponse(locationEntity)).thenReturn(locationResponse);

        RentalLocationResponse result = rentalLocationService.getById(locationId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(locationId);
    }

    @Test
    void getById_shouldThrowException_whenLocationNotFound() {
        Long locationId = 99L;
        when(rentalLocationRepository.findByIdAndActiveTrue(locationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalLocationService.getById(locationId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    //----------GetAll----------

    @Test
    void getAll_shouldReturnLocations() {
        when(rentalLocationRepository.findAllByActiveTrue()).thenReturn(List.of(locationEntity));
        when(rentalLocationMapper.toResponse(locationEntity)).thenReturn(locationResponse);

        List<RentalLocationResponse> result = rentalLocationService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(rentalLocationRepository).findAllByActiveTrue();
    }

    //----------Update----------

    @Test
    void update_shouldUpdateLocationSuccessfully() {
        Long locationId = 1L;
        when(rentalLocationRepository.findByIdAndActiveTrue(locationId)).thenReturn(Optional.of(locationEntity));
        doNothing().when(rentalLocationMapper).updateEntity(updateRequest, locationEntity);
        when(rentalLocationRepository.save(locationEntity)).thenReturn(locationEntity);
        when(rentalLocationMapper.toResponse(locationEntity)).thenReturn(locationResponse);

        RentalLocationResponse result = rentalLocationService.update(locationId, updateRequest);

        assertThat(result).isNotNull();
        verify(rentalLocationMapper).updateEntity(updateRequest, locationEntity);
        verify(rentalLocationRepository).save(locationEntity);
    }

    @Test
    void update_shouldThrowException_whenLocationNotFound() {
        Long locationId = 99L;
        when(rentalLocationRepository.findByIdAndActiveTrue(locationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalLocationService.update(locationId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    //----------Delete----------

    @Test
    void delete_shouldDeactivateLocationSuccessfully() {
        Long locationId = 1L;
        when(rentalLocationRepository.findByIdAndActiveTrue(locationId)).thenReturn(Optional.of(locationEntity));
        when(rentalLocationRepository.save(locationEntity)).thenReturn(locationEntity);

        rentalLocationService.delete(locationId);

        assertThat(locationEntity.isActive()).isFalse();
        verify(rentalLocationRepository).save(locationEntity);
    }

    @Test
    void delete_shouldThrowException_whenLocationNotFound() {
        Long locationId = 99L;
        when(rentalLocationRepository.findByIdAndActiveTrue(locationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalLocationService.delete(locationId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");

        verify(rentalLocationRepository, never()).save(any());
    }
}
