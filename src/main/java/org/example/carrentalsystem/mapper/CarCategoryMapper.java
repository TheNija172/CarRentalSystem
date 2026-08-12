package org.example.carrentalsystem.mapper;

import org.example.carrentalsystem.dto.category.CarCategoryCreateRequest;
import org.example.carrentalsystem.dto.category.CarCategoryResponse;
import org.example.carrentalsystem.dto.category.CarCategoryUpdateRequest;
import org.example.carrentalsystem.entity.CarCategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface  CarCategoryMapper {

    CarCategoryResponse toResponse(CarCategoryEntity category);

    CarCategoryEntity toEntity(CarCategoryCreateRequest request);

    void updateEntity(CarCategoryUpdateRequest request, @MappingTarget CarCategoryEntity category);
}
