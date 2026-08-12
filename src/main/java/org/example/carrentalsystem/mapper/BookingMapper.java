package org.example.carrentalsystem.mapper;

import org.example.carrentalsystem.dto.booking.BookingCreateRequest;
import org.example.carrentalsystem.dto.booking.BookingResponse;
import org.example.carrentalsystem.dto.booking.BookingUpdateRequest;
import org.example.carrentalsystem.entity.BookingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface  BookingMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "car.brand", target = "carBrand")
    @Mapping(source = "car.model", target = "carModel")
    @Mapping(source = "pickupLocation.id", target = "pickupLocationId")
    @Mapping(source = "pickupLocation.name", target = "pickupLocationName")
    @Mapping(source = "returnLocation.id", target = "returnLocationId")
    @Mapping(source = "returnLocation.name", target = "returnLocationName")
    BookingResponse toResponse(BookingEntity bookingEntity);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "car", ignore = true)
    @Mapping(target = "pickupLocation", ignore = true)
    @Mapping(target = "returnLocation", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "status", ignore = true)
    BookingEntity toEntity(BookingCreateRequest request);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "car", ignore = true)
    @Mapping(target = "pickupLocation", ignore = true)
    @Mapping(target = "returnLocation", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    void updateEntity(BookingUpdateRequest request, @MappingTarget BookingEntity bookingEntity);
}
