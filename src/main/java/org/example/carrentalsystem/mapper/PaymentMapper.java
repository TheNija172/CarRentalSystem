package org.example.carrentalsystem.mapper;

import org.example.carrentalsystem.dto.payment.PaymentCreateRequest;
import org.example.carrentalsystem.dto.payment.PaymentResponse;
import org.example.carrentalsystem.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface  PaymentMapper {

    @Mapping(source = "booking.id", target = "bookingId")
    PaymentResponse toResponse(Payment payment);

    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    Payment toEntity(PaymentCreateRequest request);

}
