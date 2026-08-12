package org.example.carrentalsystem.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalsystem.dto.booking.BookingCreateRequest;
import org.example.carrentalsystem.dto.booking.BookingResponse;
import org.example.carrentalsystem.entity.BookingEntity;
import org.example.carrentalsystem.entity.CarEntity;
import org.example.carrentalsystem.entity.RentalLocationEntity;
import org.example.carrentalsystem.entity.UserEntity;
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
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
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

        log.info(
                "Creating booking: userId={}, carId={}, startDate={}, endDate={}",
                userId,
                request.getCarId(),
                request.getStartDate(),
                request.getEndDate()
        );

        validateDates(request.getStartDate(), request.getEndDate());

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id " + userId + " not found"
                ));

        CarEntity car = carRepository.findActiveCarForUpdate(
                        request.getCarId()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Car with id "
                                + request.getCarId()
                                + " not found"
                ));

        RentalLocationEntity pickupLocation =
                rentalLocationRepository
                        .findByIdAndActiveTrue(
                                request.getPickupLocationId()
                        )
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Pickup location with id "
                                        + request.getPickupLocationId()
                                        + " not found"
                        ));

        RentalLocationEntity returnLocation =
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
            log.warn(
                    "Booking creation rejected: carId={} is already booked, userId={}",
                    car.getId(),
                    userId
            );

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

        BookingEntity booking = bookingMapper.toEntity(request);

        booking.setUser(userEntity);
        booking.setCar(car);
        booking.setPickupLocation(pickupLocation);
        booking.setReturnLocation(returnLocation);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.PENDING);

        BookingEntity savedBooking =
                bookingRepository.save(booking);

        log.info(
                "Booking created successfully: bookingId={}, userId={}, carId={}",
                savedBooking.getId(),
                userId,
                car.getId()
        );

        return bookingMapper.toResponse(savedBooking);
    }

    @Override
    public BookingResponse getById(Long id, Long userId) {

        BookingEntity bookingEntity = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking with id " + id + " not found"
                ));

        if (!bookingEntity.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(
                    "You do not have access to this booking"
            );
        }

        return bookingMapper.toResponse(bookingEntity);
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

        log.info(
                "Cancelling booking: bookingId={}, userId={}",
                id,
                userId
        );

        BookingEntity booking = bookingRepository.findById(id)
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

        log.info(
                "Booking cancelled successfully: bookingId={}",
                id
        );
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
