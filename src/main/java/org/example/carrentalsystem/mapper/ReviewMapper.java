package org.example.carrentalsystem.mapper;

import org.example.carrentalsystem.dto.review.ReviewCreateRequest;
import org.example.carrentalsystem.dto.review.ReviewResponse;
import org.example.carrentalsystem.dto.review.ReviewUpdateRequest;
import org.example.carrentalsystem.entity.ReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface  ReviewMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "car.brand", target = "carBrand")
    @Mapping(source = "car.model", target = "carModel")
    @Mapping(source = "booking.id", target = "bookingId")
    ReviewResponse toResponse(ReviewEntity reviewEntity);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "car", ignore = true)
    @Mapping(target = "booking", ignore = true)
    ReviewEntity toEntity(ReviewCreateRequest request);

    void updateEntity(ReviewUpdateRequest request, @MappingTarget ReviewEntity reviewEntity);
}
