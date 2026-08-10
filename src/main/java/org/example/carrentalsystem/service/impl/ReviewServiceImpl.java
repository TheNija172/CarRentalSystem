package org.example.carrentalsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carrentalsystem.dto.review.ReviewCreateRequest;
import org.example.carrentalsystem.dto.review.ReviewResponse;
import org.example.carrentalsystem.entity.Booking;
import org.example.carrentalsystem.entity.Review;
import org.example.carrentalsystem.entity.User;
import org.example.carrentalsystem.enums.BookingStatus;
import org.example.carrentalsystem.exception.BusinessException;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.mapper.ReviewMapper;
import org.example.carrentalsystem.repository.BookingRepository;
import org.example.carrentalsystem.repository.ReviewRepository;
import org.example.carrentalsystem.repository.UserRepository;
import org.example.carrentalsystem.service.ReviewService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewResponse create(ReviewCreateRequest request, Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("User with id " + userId + " not found"));

        Booking booking = bookingRepository.findById(request.getBookingId()).orElseThrow(() ->
                new ResourceNotFoundException("Booking with id " + request.getBookingId() + " not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new BusinessException(
                    "You cannot review another user's booking"
            );
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BusinessException(
                    "You can review only completed bookings"
            );
        }

        if (reviewRepository.existsByBookingId(
                booking.getId()
        )) {
            throw new BusinessException(
                    "Review for this booking already exists"
            );
        }

        Review review = reviewMapper.toEntity(request);

        review.setUser(user);
        review.setCar(booking.getCar());
        review.setBooking(booking);

        Review savedReview = reviewRepository.save(review);

        return reviewMapper.toResponse(savedReview);
    }

    @Override
    public ReviewResponse getById(Long id) {

        Review review = reviewRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Review with id " + id + " not found"));

        return reviewMapper.toResponse(review);
    }

    @Override
    public List<ReviewResponse> getByCarId(Long carId) {

        return reviewRepository.findByCarId(carId)
                .stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {

        Review review = reviewRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Review with id " + id + " not found"));

        if (!review.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You cannot delete this review");
        }

        reviewRepository.delete(review);
    }
}
