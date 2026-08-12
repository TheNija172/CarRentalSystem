package org.example.carrentalsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalsystem.dto.location.RentalLocationCreateRequest;
import org.example.carrentalsystem.dto.location.RentalLocationResponse;
import org.example.carrentalsystem.dto.location.RentalLocationUpdateRequest;
import org.example.carrentalsystem.entity.RentalLocationEntity;
import org.example.carrentalsystem.exception.BusinessException;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.mapper.RentalLocationMapper;
import org.example.carrentalsystem.repository.RentalLocationRepository;
import org.example.carrentalsystem.service.RentalLocationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class RentalLocationServiceImpl implements RentalLocationService {

    private final RentalLocationRepository rentalLocationRepository;
    private final RentalLocationMapper rentalLocationMapper;

    @Override
    @Transactional
    public RentalLocationResponse create(RentalLocationCreateRequest request) {

        log.info("Creating location: name={}, address={}", request.getName(), request.getAddress());

        if(rentalLocationRepository.existsByNameAndAddress(request.getName(), request.getAddress())) {

            log.warn("Rental location with this name={} and address={} already exists"
                    ,request.getName()
                    ,request.getAddress());

            throw new BusinessException("Rental location with this name and address already exists");
        }

        RentalLocationEntity rentalLocationEntity = rentalLocationMapper.toEntity(request);

        RentalLocationEntity savedLocation = rentalLocationRepository.save(rentalLocationEntity);

        log.info("Location created successfully: name={}, address={}", request.getName(), request.getAddress());

        return rentalLocationMapper.toResponse(savedLocation);
    }

    @Override
    public RentalLocationResponse getById(Long id) {

        RentalLocationEntity location = rentalLocationRepository.findByIdAndActiveTrue(id).orElseThrow(() ->
                new ResourceNotFoundException("Rental location with id " + id + " not found"));

        return rentalLocationMapper.toResponse(location);
    }

    @Override
    public List<RentalLocationResponse> getAll() {

        return rentalLocationRepository.findAllByActiveTrue()
                .stream()
                .map(rentalLocationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public RentalLocationResponse update(Long id, RentalLocationUpdateRequest request) {

        RentalLocationEntity location = rentalLocationRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rental location with id " + id + " not found"
                ));

        rentalLocationMapper.updateEntity(request, location);

        RentalLocationEntity updatedLocation = rentalLocationRepository.save(location);

        return rentalLocationMapper.toResponse(updatedLocation);
    }

    @Override
    @Transactional
    public void delete(Long id) {

        log.info("Deleting location: location id={}", id);

        RentalLocationEntity location = rentalLocationRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rental location with id " + id + " not found"
                ));

        location.setActive(false);

        log.info("Location deleted successfully: location id = {}", id);

        rentalLocationRepository.save(location);
    }
}
