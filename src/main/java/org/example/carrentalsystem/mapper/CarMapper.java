package org.example.carrentalsystem.mapper;

import org.example.carrentalsystem.dto.car.CarCreateRequest;
import org.example.carrentalsystem.dto.car.CarResponse;
import org.example.carrentalsystem.dto.car.CarUpdateRequest;
import org.example.carrentalsystem.entity.Car;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CarMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    CarResponse toResponse(Car car);

    @Mapping(target = "category", ignore = true)
    Car toEntity(CarCreateRequest request);

    @Mapping(target = "category", ignore = true)
    void updateEntity(CarUpdateRequest request, @MappingTarget Car car);
}
