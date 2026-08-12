package org.example.carrentalsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalsystem.dto.review.ReviewCreateRequest;
import org.example.carrentalsystem.dto.review.ReviewResponse;
import org.example.carrentalsystem.entity.BookingEntity;
import org.example.carrentalsystem.entity.ReviewEntity;
import org.example.carrentalsystem.entity.UserEntity;
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

@Slf4j
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

        log.info("Creating review");

        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("User with id " + userId + " not found"));

        BookingEntity booking = bookingRepository.findById(request.getBookingId()).orElseThrow(() ->
                new ResourceNotFoundException("Booking with id " + request.getBookingId() + " not found"));

        if (!booking.getUser().getId().equals(userId)) {

            log.warn("Cannot review another user's booking");

            throw new BusinessException(
                    "You cannot review another user's booking"
            );
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {

            log.warn("Incomplete bookings cannot be reviewed");

            throw new BusinessException(
                    "You can review only completed bookings"
            );
        }

        if (reviewRepository.existsByBookingId(booking.getId())) {

            log.warn("Review for this booking already exists: booking id={}", booking.getId());

            throw new BusinessException(
                    "Review for this booking already exists"
            );
        }

        ReviewEntity reviewEntity = reviewMapper.toEntity(request);

        reviewEntity.setUser(userEntity);
        reviewEntity.setCar(booking.getCar());
        reviewEntity.setBooking(booking);

        ReviewEntity savedReview = reviewRepository.save(reviewEntity);

        log.info("Review created successfully");

        return reviewMapper.toResponse(savedReview);
    }

    @Override
    public ReviewResponse getById(Long id) {

        ReviewEntity reviewEntity = reviewRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Review with id " + id + " not found"));

        return reviewMapper.toResponse(reviewEntity);
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

        log.info("Deleting review: review id={}", id);

        ReviewEntity reviewEntity = reviewRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Review with id " + id + " not found"));

        if (!reviewEntity.getUser().getId().equals(userId)) {

            log.warn("This user cannot delete this review: user id={}", userId);

            throw new AccessDeniedException("You cannot delete this review");
        }

        reviewRepository.delete(reviewEntity);

        log.info("Review deleted successfully: review id={}", id);
    }
}
