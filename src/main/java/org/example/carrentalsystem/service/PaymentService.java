package org.example.carrentalsystem.service;

import org.example.carrentalsystem.dto.payment.PaymentCreateRequest;
import org.example.carrentalsystem.dto.payment.PaymentResponse;
import org.example.carrentalsystem.enums.PaymentStatus;

import java.util.List;

public interface PaymentService {

    PaymentResponse create(PaymentCreateRequest request, Long bookingId);

    PaymentResponse getById(Long id);

    List<PaymentResponse> getByBookingId(Long bookingId);

    PaymentResponse updateStatus(Long id, PaymentStatus status);
}
