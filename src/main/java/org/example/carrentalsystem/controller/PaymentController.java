package org.example.carrentalsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carrentalsystem.dto.payment.PaymentCreateRequest;
import org.example.carrentalsystem.dto.payment.PaymentResponse;
import org.example.carrentalsystem.enums.PaymentStatus;
import org.example.carrentalsystem.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/booking/{bookingId}")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse create(@PathVariable Long bookingId, @Valid @RequestBody PaymentCreateRequest request) {

        return paymentService.create(request, bookingId);
    }

    @GetMapping("/{id}")
    public PaymentResponse getById(@PathVariable Long id) {

        return paymentService.getById(id);
    }

    @GetMapping("/booking/{bookingId}")
    public List<PaymentResponse> getByBookingId(@PathVariable Long bookingId) {

        return paymentService.getByBookingId(bookingId);
    }

    @PatchMapping("/{id}/status")
    public PaymentResponse updateStatus(@PathVariable Long id, @RequestParam PaymentStatus status) {

        return paymentService.updateStatus(id, status);
    }
}
