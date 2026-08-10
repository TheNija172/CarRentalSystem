package org.example.carrentalsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carrentalsystem.dto.location.RentalLocationCreateRequest;
import org.example.carrentalsystem.dto.location.RentalLocationResponse;
import org.example.carrentalsystem.dto.location.RentalLocationUpdateRequest;
import org.example.carrentalsystem.entity.RentalLocation;
import org.example.carrentalsystem.exception.BusinessException;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.mapper.RentalLocationMapper;
import org.example.carrentalsystem.repository.RentalLocationRepository;
import org.example.carrentalsystem.service.RentalLocationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class RentalLocationServiceImpl implements RentalLocationService {

    private final RentalLocationRepository rentalLocationRepository;
    private final RentalLocationMapper rentalLocationMapper;

    @Override
    public RentalLocationResponse create(RentalLocationCreateRequest request) {

        if(rentalLocationRepository.existsByNameAndAddress(request.getName(), request.getAddress())) {

            throw new BusinessException("Rental location with this name and address already exists");
        }

        RentalLocation rentalLocation = rentalLocationMapper.toEntity(request);

        RentalLocation savedLocation = rentalLocationRepository.save(rentalLocation);

        return rentalLocationMapper.toResponse(savedLocation);
    }

    @Override
    public RentalLocationResponse getById(Long id) {

        RentalLocation location = rentalLocationRepository.findByIdAndActiveTrue(id).orElseThrow(() ->
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
    public RentalLocationResponse update(Long id, RentalLocationUpdateRequest request) {

        RentalLocation location = rentalLocationRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rental location with id " + id + " not found"
                ));

        rentalLocationMapper.updateEntity(request, location);

        RentalLocation updatedLocation = rentalLocationRepository.save(location);

        return rentalLocationMapper.toResponse(updatedLocation);
    }

    @Override
    public void delete(Long id) {

        RentalLocation location = rentalLocationRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rental location with id " + id + " not found"
                ));

        location.setActive(false);

        rentalLocationRepository.save(location);
    }
}
