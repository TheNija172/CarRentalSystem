package org.example.carrentalsystem.service.impl;

import lombok.AllArgsConstructor;
import org.example.carrentalsystem.dto.booking.BookingCreateRequest;
import org.example.carrentalsystem.dto.booking.BookingResponse;
import org.example.carrentalsystem.dto.booking.BookingUpdateRequest;
import org.example.carrentalsystem.entity.Booking;
import org.example.carrentalsystem.entity.Car;
import org.example.carrentalsystem.entity.RentalLocation;
import org.example.carrentalsystem.entity.User;
import org.example.carrentalsystem.enums.BookingStatus;
import org.example.carrentalsystem.exception.BusinessException;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.mapper.BookingMapper;
import org.example.carrentalsystem.repository.BookingRepository;
import org.example.carrentalsystem.repository.CarRepository;
import org.example.carrentalsystem.repository.RentalLocationRepository;
import org.example.carrentalsystem.repository.UserRepository;
import org.example.carrentalsystem.service.BookingService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@AllArgsConstructor
@Transactional(readOnly = true)
@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final RentalLocationRepository rentalLocationRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponse create(BookingCreateRequest request, Long userId) {

        validateDates(request.getStartDate(), request.getEndDate());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id " + userId + " not found"
                ));

        Car car = carRepository.findByIdAndActiveTrue(
                        request.getCarId()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Car with id "
                                + request.getCarId()
                                + " not found"
                ));

        RentalLocation pickupLocation =
                rentalLocationRepository
                        .findByIdAndActiveTrue(
                                request.getPickupLocationId()
                        )
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Pickup location with id "
                                        + request.getPickupLocationId()
                                        + " not found"
                        ));

        RentalLocation returnLocation =
                rentalLocationRepository
                        .findByIdAndActiveTrue(
                                request.getReturnLocationId()
                        )
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Return location with id "
                                        + request.getReturnLocationId()
                                        + " not found"
                        ));

        boolean overlapping = bookingRepository.existsOverlappingBooking(
                        car.getId(),
                        request.getStartDate(),
                        request.getEndDate(),
                        BookingStatus.CANCELLED
                );

        if (overlapping) {
            throw new BusinessException(
                    "Car is already booked for the selected period"
            );
        }

        long rentalDays = calculateRentalDays(
                request.getStartDate(),
                request.getEndDate()
        );

        BigDecimal totalPrice = car.getCategory()
                .getPricePerDay()
                .multiply(BigDecimal.valueOf(rentalDays));

        Booking booking = bookingMapper.toEntity(request);

        booking.setUser(user);
        booking.setCar(car);
        booking.setPickupLocation(pickupLocation);
        booking.setReturnLocation(returnLocation);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.PENDING);

        Booking savedBooking =
                bookingRepository.save(booking);

        return bookingMapper.toResponse(savedBooking);
    }

    @Override
    public BookingResponse getById(Long id, Long userId) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking with id " + id + " not found"
                ));

        if (!booking.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(
                    "You do not have access to this booking"
            );
        }

        return bookingMapper.toResponse(booking);
    }

    @Override
    public List<BookingResponse> getAll() {

        return bookingRepository.findAll()
                .stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @Override
    public List<BookingResponse> getByUserId(Long userId) {

        if (!userRepository.existsById(userId)) {

            throw new ResourceNotFoundException("User with id " + userId + " not found");
        }

        return bookingRepository.findByUserId(userId)
                .stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void cancel(Long id, Long userId) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking with id " + id + " not found"
                ));

        if (!booking.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(
                    "You cannot cancel this booking"
            );
        }

        if (booking.getStatus() != BookingStatus.PENDING
                && booking.getStatus() != BookingStatus.CONFIRMED) {

            throw new BusinessException(
                    "Booking cannot be cancelled"
            );
        }

        booking.setStatus(BookingStatus.CANCELLED);
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {

        if (startDate == null || endDate == null) {

            throw new BusinessException("Start date and end date are required");
        }

        if (!endDate.isAfter(startDate)) {

            throw new BusinessException("End date must be after start date");
        }

        if (startDate.isBefore(LocalDate.now())) {

            throw new BusinessException("Start date cannot be in the past");
        }
    }

    private long calculateRentalDays(LocalDate startDate, LocalDate endDate) {

        return ChronoUnit.DAYS.between(startDate, endDate);
    }
}
