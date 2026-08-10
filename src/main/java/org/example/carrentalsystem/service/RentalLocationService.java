package org.example.carrentalsystem.service;

import org.example.carrentalsystem.dto.location.RentalLocationCreateRequest;
import org.example.carrentalsystem.dto.location.RentalLocationResponse;
import org.example.carrentalsystem.dto.location.RentalLocationUpdateRequest;

import java.util.List;

public interface RentalLocationService {

    public RentalLocationResponse create(RentalLocationCreateRequest request);

    public RentalLocationResponse getById(Long id);

    public List<RentalLocationResponse> getAll();

    public RentalLocationResponse update(Long id, RentalLocationUpdateRequest request);

    void delete(Long id);
}
