package org.example.carrentalsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalsystem.dto.payment.PaymentCreateRequest;
import org.example.carrentalsystem.dto.payment.PaymentResponse;
import org.example.carrentalsystem.entity.BookingEntity;
import org.example.carrentalsystem.entity.PaymentEntity;
import org.example.carrentalsystem.enums.BookingStatus;
import org.example.carrentalsystem.enums.PaymentStatus;
import org.example.carrentalsystem.exception.BusinessException;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.mapper.PaymentMapper;
import org.example.carrentalsystem.repository.BookingRepository;
import org.example.carrentalsystem.repository.PaymentRepository;
import org.example.carrentalsystem.service.PaymentService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentResponse create(PaymentCreateRequest request, Long bookingId, Long userId) {

        log.info(
                "Creating payment: bookingId={}, userId={}",
                bookingId,
                userId
        );

        BookingEntity booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ResourceNotFoundException("Booking with id " + bookingId + " not found"));

        if (!booking.getUser().getId().equals(userId)) {

            log.warn(
                    "Payment creation denied: userId={} tried to pay for bookingId={}",
                    userId,
                    bookingId
            );

            throw new AccessDeniedException("You cannot create payment for another user's booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {

            throw new BusinessException("Cannot create payment for cancelled booking");
        }

        PaymentEntity payment = paymentMapper.toEntity(request);

        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaidAt(null);

        PaymentEntity savedPayment = paymentRepository.save(payment);

        log.info(
                "Payment created successfully: paymentId={}, bookingId={}",
                savedPayment.getId(),
                bookingId
        );

        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    public PaymentResponse getById(Long id, Long userId) {

        PaymentEntity paymentEntity = paymentRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Payment with id " + id + " not found"));

        if (!paymentEntity.getBooking().getUser().getId().equals(userId)) {

            throw new AccessDeniedException("You do not have access to this payment");
        }

        return paymentMapper.toResponse(paymentEntity);
    }

    @Override
    public List<PaymentResponse> getByBookingId(Long bookingId, Long userId) {

        BookingEntity bookingEntity = bookingRepository.findById(bookingId).orElseThrow(()
                -> new ResourceNotFoundException("Booking with id " + bookingId + " not found"));

        if (!bookingEntity.getUser().getId().equals(userId)) {

            throw new AccessDeniedException("You do not have access to payments for this booking");
        }

        return paymentRepository.findByBookingId(bookingId)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PaymentResponse updateStatus(
            Long id,
            PaymentStatus status
    ) {

        PaymentEntity paymentEntity = paymentRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Payment with id " + id + " not found"));

        paymentEntity.setStatus(status);

        if (status == PaymentStatus.COMPLETED) {
            paymentEntity.setPaidAt(LocalDateTime.now());
        }

        return paymentMapper.toResponse(paymentEntity);
    }
}
