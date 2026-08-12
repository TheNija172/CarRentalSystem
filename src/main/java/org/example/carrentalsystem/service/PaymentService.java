package org.example.carrentalsystem.service;

import org.example.carrentalsystem.dto.payment.PaymentCreateRequest;
import org.example.carrentalsystem.dto.payment.PaymentResponse;
import org.example.carrentalsystem.enums.PaymentStatus;

import java.util.List;

public interface PaymentService {

    PaymentResponse create(PaymentCreateRequest request, Long bookingId, Long userId);

    PaymentResponse getById(Long id, Long userId);

    List<PaymentResponse> getByBookingId(Long bookingId, Long userId);

    PaymentResponse updateStatus(Long id, PaymentStatus status);
}
