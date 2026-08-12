package org.example.carrentalsystem.service;

import org.example.carrentalsystem.dto.payment.PaymentCreateRequest;
import org.example.carrentalsystem.dto.payment.PaymentResponse;
import org.example.carrentalsystem.entity.BookingEntity;
import org.example.carrentalsystem.entity.PaymentEntity;
import org.example.carrentalsystem.entity.UserEntity;
import org.example.carrentalsystem.enums.BookingStatus;
import org.example.carrentalsystem.enums.PaymentMethod;
import org.example.carrentalsystem.enums.PaymentStatus;
import org.example.carrentalsystem.exception.BusinessException;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.mapper.PaymentMapper;
import org.example.carrentalsystem.repository.BookingRepository;
import org.example.carrentalsystem.repository.PaymentRepository;
import org.example.carrentalsystem.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    //----------Create----------
    @Test
    void create_shouldCreatePaymentSuccessfully() {

        Long paymentId = 1L;
        Long bookingId = 2L;
        Long userId = 3L;

        BigDecimal totalPrice = new BigDecimal("150.00");

        PaymentCreateRequest request = new PaymentCreateRequest();
        request.setPaymentMethod(PaymentMethod.CARD);

        UserEntity user = new UserEntity();
        user.setId(userId);

        BookingEntity booking = new BookingEntity();
        booking.setId(bookingId);
        booking.setUser(user);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalPrice(totalPrice);

        PaymentEntity paymentEntity = new PaymentEntity();

        PaymentEntity savedPayment = new PaymentEntity();
        savedPayment.setId(paymentId);

        PaymentResponse response = new PaymentResponse();
        response.setId(paymentId);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(paymentMapper.toEntity(request))
                .thenReturn(paymentEntity);

        when(paymentRepository.save(paymentEntity))
                .thenReturn(savedPayment);

        when(paymentMapper.toResponse(savedPayment))
                .thenReturn(response);

        PaymentResponse result =
                paymentService.create(request, bookingId, userId);

        assertNotNull(result);
        assertEquals(paymentId, result.getId());

        assertEquals(booking, paymentEntity.getBooking());
        assertEquals(totalPrice, paymentEntity.getAmount());
        assertEquals(PaymentStatus.PENDING, paymentEntity.getStatus());
        assertNull(paymentEntity.getPaidAt());

        verify(paymentRepository).save(paymentEntity);
        verify(paymentMapper).toResponse(savedPayment);
    }


    @Test
    void create_shouldThrowException_whenBookingNotFound() {

        Long bookingId = 2L;
        Long userId = 3L;

        PaymentCreateRequest request = new PaymentCreateRequest();

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.create(request, bookingId, userId)
        );

        assertEquals(
                "Booking with id " + bookingId + " not found",
                exception.getMessage()
        );

        verifyNoInteractions(
                paymentRepository,
                paymentMapper
        );
    }


    @Test
    void create_shouldThrowException_whenBookingBelongsToAnotherUser() {

        Long bookingId = 2L;
        Long ownerId = 3L;
        Long anotherUserId = 4L;

        UserEntity owner = new UserEntity();
        owner.setId(ownerId);

        BookingEntity booking = new BookingEntity();
        booking.setId(bookingId);
        booking.setUser(owner);
        booking.setStatus(BookingStatus.CONFIRMED);

        PaymentCreateRequest request = new PaymentCreateRequest();

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> paymentService.create(
                        request,
                        bookingId,
                        anotherUserId
                )
        );

        assertEquals(
                "You cannot create payment for another user's booking",
                exception.getMessage()
        );

        verifyNoInteractions(
                paymentRepository,
                paymentMapper
        );
    }


    @Test
    void create_shouldThrowException_whenBookingIsCancelled() {

        Long bookingId = 2L;
        Long userId = 3L;

        UserEntity user = new UserEntity();
        user.setId(userId);

        BookingEntity booking = new BookingEntity();
        booking.setId(bookingId);
        booking.setUser(user);
        booking.setStatus(BookingStatus.CANCELLED);

        PaymentCreateRequest request = new PaymentCreateRequest();

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentService.create(
                        request,
                        bookingId,
                        userId
                )
        );

        assertEquals(
                "Cannot create payment for cancelled booking",
                exception.getMessage()
        );

        verifyNoInteractions(
                paymentRepository,
                paymentMapper
        );
    }

    //----------GetById----------
    @Test
    void getById_shouldReturnPayment() {

        Long paymentId = 1L;
        Long userId = 2L;

        UserEntity user = new UserEntity();
        user.setId(userId);

        BookingEntity booking = new BookingEntity();
        booking.setUser(user);

        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setId(paymentId);
        paymentEntity.setBooking(booking);

        PaymentResponse response = new PaymentResponse();
        response.setId(paymentId);

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(paymentEntity));

        when(paymentMapper.toResponse(paymentEntity))
                .thenReturn(response);

        PaymentResponse result =
                paymentService.getById(paymentId, userId);

        assertNotNull(result);
        assertEquals(paymentId, result.getId());

        verify(paymentMapper).toResponse(paymentEntity);
    }


    @Test
    void getById_shouldThrowException_whenPaymentNotFound() {

        Long paymentId = 1L;
        Long userId = 2L;

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.getById(paymentId, userId)
        );

        assertEquals(
                "Payment with id " + paymentId + " not found",
                exception.getMessage()
        );

        verifyNoInteractions(paymentMapper);
    }


    @Test
    void getById_shouldThrowException_whenPaymentBelongsToAnotherUser() {

        Long paymentId = 1L;
        Long ownerId = 2L;
        Long anotherUserId = 3L;

        UserEntity owner = new UserEntity();
        owner.setId(ownerId);

        BookingEntity booking = new BookingEntity();
        booking.setUser(owner);

        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setId(paymentId);
        paymentEntity.setBooking(booking);

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(paymentEntity));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> paymentService.getById(
                        paymentId,
                        anotherUserId
                )
        );

        assertEquals(
                "You do not have access to this payment",
                exception.getMessage()
        );

        verifyNoInteractions(paymentMapper);
    }

    //----------GetByBookingId----------
    @Test
    void getByBookingId_shouldReturnPayments() {

        Long bookingId = 1L;
        Long userId = 2L;

        UserEntity user = new UserEntity();
        user.setId(userId);

        BookingEntity booking = new BookingEntity();
        booking.setId(bookingId);
        booking.setUser(user);

        PaymentEntity payment1 = new PaymentEntity();
        payment1.setId(10L);
        payment1.setBooking(booking);

        PaymentEntity payment2 = new PaymentEntity();
        payment2.setId(20L);
        payment2.setBooking(booking);

        PaymentResponse response1 = new PaymentResponse();
        response1.setId(10L);

        PaymentResponse response2 = new PaymentResponse();
        response2.setId(20L);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.findByBookingId(bookingId))
                .thenReturn(List.of(payment1, payment2));

        when(paymentMapper.toResponse(payment1))
                .thenReturn(response1);

        when(paymentMapper.toResponse(payment2))
                .thenReturn(response2);

        List<PaymentResponse> result =
                paymentService.getByBookingId(bookingId, userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).getId());
        assertEquals(20L, result.get(1).getId());

        verify(paymentRepository).findByBookingId(bookingId);
    }


    @Test
    void getByBookingId_shouldThrowException_whenBookingNotFound() {

        Long bookingId = 1L;
        Long userId = 2L;

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.getByBookingId(
                        bookingId,
                        userId
                )
        );

        assertEquals(
                "Booking with id " + bookingId + " not found",
                exception.getMessage()
        );

        verifyNoInteractions(
                paymentRepository,
                paymentMapper
        );
    }


    @Test
    void getByBookingId_shouldThrowException_whenBookingBelongsToAnotherUser() {

        Long bookingId = 1L;
        Long ownerId = 2L;
        Long anotherUserId = 3L;

        UserEntity owner = new UserEntity();
        owner.setId(ownerId);

        BookingEntity booking = new BookingEntity();
        booking.setId(bookingId);
        booking.setUser(owner);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> paymentService.getByBookingId(
                        bookingId,
                        anotherUserId
                )
        );

        assertEquals(
                "You do not have access to payments for this booking",
                exception.getMessage()
        );

        verifyNoInteractions(
                paymentRepository,
                paymentMapper
        );
    }

    ////----------UpdateStatus----------
    @Test
    void updateStatus_shouldUpdateStatusSuccessfully() {

        Long paymentId = 1L;

        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setId(paymentId);
        paymentEntity.setStatus(PaymentStatus.PENDING);

        PaymentResponse response = new PaymentResponse();
        response.setId(paymentId);

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(paymentEntity));

        when(paymentMapper.toResponse(paymentEntity))
                .thenReturn(response);

        PaymentResponse result =
                paymentService.updateStatus(
                        paymentId,
                        PaymentStatus.FAILED
                );

        assertNotNull(result);
        assertEquals(PaymentStatus.FAILED, paymentEntity.getStatus());
        assertNull(paymentEntity.getPaidAt());

        verify(paymentMapper).toResponse(paymentEntity);
    }


    @Test
    void updateStatus_shouldSetPaidAt_whenStatusIsCompleted() {

        Long paymentId = 1L;

        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setId(paymentId);
        paymentEntity.setStatus(PaymentStatus.PENDING);
        paymentEntity.setPaidAt(null);

        PaymentResponse response = new PaymentResponse();
        response.setId(paymentId);

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(paymentEntity));

        when(paymentMapper.toResponse(paymentEntity))
                .thenReturn(response);

        PaymentResponse result =
                paymentService.updateStatus(
                        paymentId,
                        PaymentStatus.COMPLETED
                );

        assertNotNull(result);
        assertEquals(
                PaymentStatus.COMPLETED,
                paymentEntity.getStatus()
        );
        assertNotNull(paymentEntity.getPaidAt());

        verify(paymentMapper).toResponse(paymentEntity);
    }


    @Test
    void updateStatus_shouldThrowException_whenPaymentNotFound() {

        Long paymentId = 1L;

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.updateStatus(
                        paymentId,
                        PaymentStatus.COMPLETED
                )
        );

        assertEquals(
                "Payment with id " + paymentId + " not found",
                exception.getMessage()
        );

        verifyNoInteractions(paymentMapper);
    }
}