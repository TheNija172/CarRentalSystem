package org.example.carrentalsystem.mapper;

import org.example.carrentalsystem.dto.location.RentalLocationCreateRequest;
import org.example.carrentalsystem.dto.location.RentalLocationResponse;
import org.example.carrentalsystem.dto.location.RentalLocationUpdateRequest;
import org.example.carrentalsystem.entity.RentalLocation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.TargetPropertyName;

@Mapper(componentModel = "spring")
public interface  RentalLocationMapper {

    RentalLocationResponse toResponse(RentalLocation location);

    RentalLocation toEntity(RentalLocationCreateRequest request);

    void updateEntity(RentalLocationUpdateRequest request, @MappingTarget RentalLocation location);
}
