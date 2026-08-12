package org.example.carrentalsystem.service;

import org.example.carrentalsystem.dto.review.ReviewCreateRequest;
import org.example.carrentalsystem.dto.review.ReviewResponse;
import org.example.carrentalsystem.entity.BookingEntity;
import org.example.carrentalsystem.entity.CarEntity;
import org.example.carrentalsystem.entity.ReviewEntity;
import org.example.carrentalsystem.entity.UserEntity;
import org.example.carrentalsystem.enums.BookingStatus;
import org.example.carrentalsystem.exception.BusinessException;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.mapper.ReviewMapper;
import org.example.carrentalsystem.repository.BookingRepository;
import org.example.carrentalsystem.repository.ReviewRepository;
import org.example.carrentalsystem.repository.UserRepository;
import org.example.carrentalsystem.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    //----------Create----------
    @Test
    void create_shouldCreateReviewSuccessfully() {

        Long userId = 1L;
        Long bookingId = 2L;
        Long carId = 3L;

        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setRating(5);
        request.setComment("Excellent car!");
        request.setBookingId(bookingId);

        UserEntity user = new UserEntity();
        user.setId(userId);

        CarEntity car = new CarEntity();
        car.setId(carId);

        BookingEntity booking = new BookingEntity();
        booking.setId(bookingId);
        booking.setUser(user);
        booking.setCar(car);
        booking.setStatus(BookingStatus.COMPLETED);

        ReviewEntity reviewEntity = new ReviewEntity();

        ReviewEntity savedReview = new ReviewEntity();
        savedReview.setId(10L);

        ReviewResponse response = new ReviewResponse();
        response.setId(10L);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(reviewRepository.existsByBookingId(bookingId))
                .thenReturn(false);

        when(reviewMapper.toEntity(request))
                .thenReturn(reviewEntity);

        when(reviewRepository.save(reviewEntity))
                .thenReturn(savedReview);

        when(reviewMapper.toResponse(savedReview))
                .thenReturn(response);

        ReviewResponse result =
                reviewService.create(request, userId);

        assertNotNull(result);
        assertEquals(10L, result.getId());

        assertEquals(user, reviewEntity.getUser());
        assertEquals(car, reviewEntity.getCar());
        assertEquals(booking, reviewEntity.getBooking());

        verify(reviewRepository).save(reviewEntity);
        verify(reviewMapper).toResponse(savedReview);
    }


    @Test
    void create_shouldThrowException_whenUserNotFound() {

        Long userId = 1L;
        Long bookingId = 2L;

        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setBookingId(bookingId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> reviewService.create(request, userId)
        );

        assertEquals(
                "User with id " + userId + " not found",
                exception.getMessage()
        );

        verifyNoInteractions(
                bookingRepository,
                reviewRepository,
                reviewMapper
        );
    }


    @Test
    void create_shouldThrowException_whenBookingNotFound() {

        Long userId = 1L;
        Long bookingId = 2L;

        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setBookingId(bookingId);

        UserEntity user = new UserEntity();
        user.setId(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> reviewService.create(request, userId)
        );

        assertEquals(
                "Booking with id " + bookingId + " not found",
                exception.getMessage()
        );

        verifyNoInteractions(
                reviewRepository,
                reviewMapper
        );
    }


    @Test
    void create_shouldThrowException_whenBookingBelongsToAnotherUser() {

        Long userId = 1L;
        Long bookingOwnerId = 2L;
        Long bookingId = 3L;

        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setBookingId(bookingId);

        UserEntity owner = new UserEntity();
        owner.setId(bookingOwnerId);

        UserEntity currentUser = new UserEntity();
        currentUser.setId(userId);

        BookingEntity booking = new BookingEntity();
        booking.setId(bookingId);
        booking.setUser(owner);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(currentUser));

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reviewService.create(request, userId)
        );

        assertEquals(
                "You cannot review another user's booking",
                exception.getMessage()
        );

        verifyNoInteractions(
                reviewRepository,
                reviewMapper
        );
    }


    @Test
    void create_shouldThrowException_whenBookingIsNotCompleted() {

        Long userId = 1L;
        Long bookingId = 2L;

        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setBookingId(bookingId);

        UserEntity user = new UserEntity();
        user.setId(userId);

        BookingEntity booking = new BookingEntity();
        booking.setId(bookingId);
        booking.setUser(user);
        booking.setStatus(BookingStatus.CONFIRMED);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reviewService.create(request, userId)
        );

        assertEquals(
                "You can review only completed bookings",
                exception.getMessage()
        );

        verifyNoInteractions(
                reviewRepository,
                reviewMapper
        );
    }


    @Test
    void create_shouldThrowException_whenReviewAlreadyExists() {

        Long userId = 1L;
        Long bookingId = 2L;

        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setBookingId(bookingId);

        UserEntity user = new UserEntity();
        user.setId(userId);

        BookingEntity booking = new BookingEntity();
        booking.setId(bookingId);
        booking.setUser(user);
        booking.setStatus(BookingStatus.COMPLETED);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(reviewRepository.existsByBookingId(bookingId))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reviewService.create(request, userId)
        );

        assertEquals(
                "Review for this booking already exists",
                exception.getMessage()
        );

        verify(reviewRepository)
                .existsByBookingId(bookingId);

        verify(reviewRepository, never())
                .save(any());

        verifyNoInteractions(reviewMapper);
    }

    //----------GetById----------
    @Test
    void getById_shouldReturnReview() {

        Long reviewId = 1L;

        ReviewEntity reviewEntity = new ReviewEntity();
        reviewEntity.setId(reviewId);

        ReviewResponse response = new ReviewResponse();
        response.setId(reviewId);

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(reviewEntity));

        when(reviewMapper.toResponse(reviewEntity))
                .thenReturn(response);

        ReviewResponse result = reviewService.getById(reviewId);

        assertNotNull(result);
        assertEquals(reviewId, result.getId());

        verify(reviewMapper).toResponse(reviewEntity);
    }


    @Test
    void getById_shouldThrowException_whenReviewNotFound() {

        Long reviewId = 1L;

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> reviewService.getById(reviewId)
        );

        assertEquals(
                "Review with id " + reviewId + " not found",
                exception.getMessage()
        );

        verifyNoInteractions(reviewMapper);
    }

    //----------GetByCardId----------
    @Test
    void getByCarId_shouldReturnReviews() {

        Long carId = 1L;

        ReviewEntity review1 = new ReviewEntity();
        review1.setId(10L);

        ReviewEntity review2 = new ReviewEntity();
        review2.setId(20L);

        ReviewResponse response1 = new ReviewResponse();
        response1.setId(10L);

        ReviewResponse response2 = new ReviewResponse();
        response2.setId(20L);

        when(reviewRepository.findByCarId(carId))
                .thenReturn(List.of(review1, review2));

        when(reviewMapper.toResponse(review1))
                .thenReturn(response1);

        when(reviewMapper.toResponse(review2))
                .thenReturn(response2);

        List<ReviewResponse> result =
                reviewService.getByCarId(carId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).getId());
        assertEquals(20L, result.get(1).getId());

        verify(reviewRepository).findByCarId(carId);
    }

    //----------Delete----------
    @Test
    void delete_shouldDeleteReviewSuccessfully() {

        Long reviewId = 1L;
        Long userId = 2L;

        UserEntity user = new UserEntity();
        user.setId(userId);

        ReviewEntity reviewEntity = new ReviewEntity();
        reviewEntity.setId(reviewId);
        reviewEntity.setUser(user);

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(reviewEntity));

        reviewService.delete(reviewId, userId);

        verify(reviewRepository).delete(reviewEntity);
    }


    @Test
    void delete_shouldThrowException_whenReviewBelongsToAnotherUser() {

        Long reviewId = 1L;
        Long ownerId = 2L;
        Long anotherUserId = 3L;

        UserEntity owner = new UserEntity();
        owner.setId(ownerId);

        ReviewEntity reviewEntity = new ReviewEntity();
        reviewEntity.setId(reviewId);
        reviewEntity.setUser(owner);

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(reviewEntity));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> reviewService.delete(reviewId, anotherUserId)
        );

        assertEquals(
                "You cannot delete this review",
                exception.getMessage()
        );

        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowException_whenReviewNotFound() {

        Long reviewId = 1L;
        Long userId = 2L;

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> reviewService.delete(reviewId, userId)
        );

        assertEquals(
                "Review with id " + reviewId + " not found",
                exception.getMessage()
        );

        verify(reviewRepository, never()).delete(any());
    }
}
