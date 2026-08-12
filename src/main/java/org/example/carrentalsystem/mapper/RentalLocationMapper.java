package org.example.carrentalsystem.mapper;

import org.example.carrentalsystem.dto.location.RentalLocationCreateRequest;
import org.example.carrentalsystem.dto.location.RentalLocationResponse;
import org.example.carrentalsystem.dto.location.RentalLocationUpdateRequest;
import org.example.carrentalsystem.entity.RentalLocationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface  RentalLocationMapper {

    RentalLocationResponse toResponse(RentalLocationEntity location);

    RentalLocationEntity toEntity(RentalLocationCreateRequest request);

    void updateEntity(RentalLocationUpdateRequest request, @MappingTarget RentalLocationEntity location);
}
