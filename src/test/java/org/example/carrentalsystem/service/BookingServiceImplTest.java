package org.example.carrentalsystem.service;

import org.example.carrentalsystem.dto.booking.BookingCreateRequest;
import org.example.carrentalsystem.dto.booking.BookingResponse;
import org.example.carrentalsystem.entity.*;
import org.example.carrentalsystem.enums.BookingStatus;
import org.example.carrentalsystem.exception.BusinessException;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.mapper.BookingMapper;
import org.example.carrentalsystem.repository.BookingRepository;
import org.example.carrentalsystem.repository.CarRepository;
import org.example.carrentalsystem.repository.RentalLocationRepository;
import org.example.carrentalsystem.repository.UserRepository;
import org.example.carrentalsystem.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RentalLocationRepository rentalLocationRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;


    //----------Create----------
    @Test
    void create_shouldCreateBookingSuccessfully() {

        Long userId = 1L;
        Long carId = 2L;
        Long pickupLocationId = 3L;
        Long returnLocationId = 4L;

        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = startDate.plusDays(3);

        BookingCreateRequest request = new BookingCreateRequest();
        request.setCarId(carId);
        request.setPickupLocationId(pickupLocationId);
        request.setReturnLocationId(returnLocationId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);

        UserEntity user = new UserEntity();
        user.setId(userId);

        CarCategoryEntity category = new CarCategoryEntity();
        category.setPricePerDay(new BigDecimal("50.00"));

        CarEntity car = new CarEntity();
        car.setId(carId);
        car.setActive(true);
        car.setCategory(category);

        RentalLocationEntity pickupLocation = new RentalLocationEntity();
        pickupLocation.setId(pickupLocationId);
        pickupLocation.setActive(true);

        RentalLocationEntity returnLocation = new RentalLocationEntity();
        returnLocation.setId(returnLocationId);
        returnLocation.setActive(true);

        BookingEntity booking = new BookingEntity();

        BookingEntity savedBooking = new BookingEntity();
        savedBooking.setId(10L);
        savedBooking.setUser(user);
        savedBooking.setCar(car);
        savedBooking.setPickupLocation(pickupLocation);
        savedBooking.setReturnLocation(returnLocation);
        savedBooking.setTotalPrice(new BigDecimal("150.00"));
        savedBooking.setStatus(BookingStatus.PENDING);

        BookingResponse response = new BookingResponse();
        response.setId(10L);
        response.setUserId(userId);
        response.setCarId(carId);
        response.setTotalPrice(new BigDecimal("150.00"));
        response.setStatus(BookingStatus.PENDING);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(carRepository.findActiveCarForUpdate(carId))
                .thenReturn(Optional.of(car));

        when(rentalLocationRepository.findByIdAndActiveTrue(pickupLocationId))
                .thenReturn(Optional.of(pickupLocation));

        when(rentalLocationRepository.findByIdAndActiveTrue(returnLocationId))
                .thenReturn(Optional.of(returnLocation));

        when(bookingRepository.existsOverlappingBooking(
                carId,
                startDate,
                endDate,
                BookingStatus.CANCELLED
        )).thenReturn(false);

        when(bookingMapper.toEntity(request))
                .thenReturn(booking);

        when(bookingRepository.save(booking))
                .thenReturn(savedBooking);

        when(bookingMapper.toResponse(savedBooking))
                .thenReturn(response);

        BookingResponse result = bookingService.create(request, userId);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(carId, result.getCarId());
        assertEquals(new BigDecimal("150.00"), result.getTotalPrice());
        assertEquals(BookingStatus.PENDING, result.getStatus());

        verify(bookingRepository).save(booking);
        verify(bookingMapper).toResponse(savedBooking);
    }

    @Test
    void create_shouldThrowException_whenCarAlreadyBooked() {

        Long userId = 1L;
        Long carId = 2L;
        Long pickupLocationId = 3L;
        Long returnLocationId = 4L;

        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = startDate.plusDays(3);

        BookingCreateRequest request = new BookingCreateRequest();
        request.setCarId(carId);
        request.setPickupLocationId(pickupLocationId);
        request.setReturnLocationId(returnLocationId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);

        UserEntity user = new UserEntity();
        user.setId(userId);

        CarCategoryEntity category = new CarCategoryEntity();
        category.setPricePerDay(new BigDecimal("50.00"));

        CarEntity car = new CarEntity();
        car.setId(carId);
        car.setActive(true);
        car.setCategory(category);

        RentalLocationEntity pickupLocation = new RentalLocationEntity();
        pickupLocation.setId(pickupLocationId);
        pickupLocation.setActive(true);

        RentalLocationEntity returnLocation = new RentalLocationEntity();
        returnLocation.setId(returnLocationId);
        returnLocation.setActive(true);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(carRepository.findActiveCarForUpdate(carId))
                .thenReturn(Optional.of(car));

        when(rentalLocationRepository.findByIdAndActiveTrue(pickupLocationId))
                .thenReturn(Optional.of(pickupLocation));

        when(rentalLocationRepository.findByIdAndActiveTrue(returnLocationId))
                .thenReturn(Optional.of(returnLocation));

        when(bookingRepository.existsOverlappingBooking(
                carId,
                startDate,
                endDate,
                BookingStatus.CANCELLED
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bookingService.create(request, userId)
        );

        assertEquals(
                "Car is already booked for the selected period",
                exception.getMessage()
        );

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowException_whenEndDateIsBeforeStartDate() {

        Long userId = 1L;

        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = startDate.minusDays(1);

        BookingCreateRequest request = new BookingCreateRequest();
        request.setStartDate(startDate);
        request.setEndDate(endDate);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bookingService.create(request, userId)
        );

        assertEquals(
                "End date must be after start date",
                exception.getMessage()
        );

        verifyNoInteractions(
                userRepository,
                carRepository,
                rentalLocationRepository,
                bookingRepository,
                bookingMapper
        );
    }

    @Test
    void create_shouldThrowException_whenStartDateIsInThePast() {

        Long userId = 1L;

        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(2);

        BookingCreateRequest request = new BookingCreateRequest();
        request.setStartDate(startDate);
        request.setEndDate(endDate);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bookingService.create(request, userId)
        );

        assertEquals(
                "Start date cannot be in the past",
                exception.getMessage()
        );

        verifyNoInteractions(
                userRepository,
                carRepository,
                rentalLocationRepository,
                bookingRepository,
                bookingMapper
        );
    }

    @Test
    void create_shouldThrowException_whenUserNotFound() {

        Long userId = 1L;

        BookingCreateRequest request = new BookingCreateRequest();
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.create(request, userId)
        );

        assertEquals(
                "User with id " + userId + " not found",
                exception.getMessage()
        );

        verifyNoInteractions(
                carRepository,
                rentalLocationRepository,
                bookingRepository,
                bookingMapper
        );
    }

    @Test
    void create_shouldThrowException_whenCarNotFound() {

        Long userId = 1L;
        Long carId = 2L;

        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = startDate.plusDays(3);

        BookingCreateRequest request = new BookingCreateRequest();
        request.setCarId(carId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);

        UserEntity user = new UserEntity();
        user.setId(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(carRepository.findActiveCarForUpdate(carId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.create(request, userId)
        );

        assertEquals(
                "Car with id " + carId + " not found",
                exception.getMessage()
        );

        verifyNoInteractions(
                rentalLocationRepository,
                bookingRepository,
                bookingMapper
        );
    }

    @Test
    void create_shouldThrowException_whenPickupLocationNotFound() {

        Long userId = 1L;
        Long carId = 2L;
        Long pickupLocationId = 3L;
        Long returnLocationId = 4L;

        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = startDate.plusDays(3);

        BookingCreateRequest request = new BookingCreateRequest();
        request.setCarId(carId);
        request.setPickupLocationId(pickupLocationId);
        request.setReturnLocationId(returnLocationId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);

        UserEntity user = new UserEntity();
        user.setId(userId);

        CarEntity car = new CarEntity();
        car.setId(carId);

        RentalLocationEntity pickupLocation = new RentalLocationEntity();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(carRepository.findActiveCarForUpdate(carId))
                .thenReturn(Optional.of(car));

        when(rentalLocationRepository.findByIdAndActiveTrue(pickupLocationId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.create(request, userId)
        );

        assertEquals(
                "Pickup location with id " + pickupLocationId + " not found",
                exception.getMessage()
        );

        verify(rentalLocationRepository)
                .findByIdAndActiveTrue(pickupLocationId);

        verifyNoInteractions(bookingRepository, bookingMapper);
    }

    @Test
    void create_shouldThrowException_whenReturnLocationNotFound() {

        Long userId = 1L;
        Long carId = 2L;
        Long pickupLocationId = 3L;
        Long returnLocationId = 4L;

        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = startDate.plusDays(3);

        BookingCreateRequest request = new BookingCreateRequest();
        request.setCarId(carId);
        request.setPickupLocationId(pickupLocationId);
        request.setReturnLocationId(returnLocationId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);

        UserEntity user = new UserEntity();
        user.setId(userId);

        CarEntity car = new CarEntity();
        car.setId(carId);

        RentalLocationEntity pickupLocation = new RentalLocationEntity();
        pickupLocation.setId(pickupLocationId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(carRepository.findActiveCarForUpdate(carId))
                .thenReturn(Optional.of(car));

        when(rentalLocationRepository.findByIdAndActiveTrue(pickupLocationId))
                .thenReturn(Optional.of(pickupLocation));

        when(rentalLocationRepository.findByIdAndActiveTrue(returnLocationId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.create(request, userId)
        );

        assertEquals(
                "Return location with id " + returnLocationId + " not found",
                exception.getMessage()
        );

        verifyNoInteractions(bookingRepository, bookingMapper);
    }

    //----------Cancel----------
    @Test
    void cancel_shouldChangeStatusToCancelled() {

        Long bookingId = 1L;
        Long userId = 2L;

        UserEntity user = new UserEntity();
        user.setId(userId);

        BookingEntity booking = new BookingEntity();
        booking.setId(bookingId);
        booking.setUser(user);
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        bookingService.cancel(bookingId, userId);

        assertEquals(
                BookingStatus.CANCELLED,
                booking.getStatus()
        );
    }

    @Test
    void cancel_shouldThrowException_whenBookingBelongsToAnotherUser() {

        Long bookingId = 1L;
        Long ownerId = 2L;
        Long anotherUserId = 3L;

        UserEntity owner = new UserEntity();
        owner.setId(ownerId);

        BookingEntity booking = new BookingEntity();
        booking.setId(bookingId);
        booking.setUser(owner);
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> bookingService.cancel(bookingId, anotherUserId)
        );

        assertEquals(
                "You cannot cancel this booking",
                exception.getMessage()
        );

        assertEquals(
                BookingStatus.PENDING,
                booking.getStatus()
        );
    }

    @Test
    void cancel_shouldThrowException_whenBookingCannotBeCancelled() {

        Long bookingId = 1L;
        Long userId = 2L;

        UserEntity user = new UserEntity();
        user.setId(userId);

        BookingEntity booking = new BookingEntity();
        booking.setId(bookingId);
        booking.setUser(user);
        booking.setStatus(BookingStatus.COMPLETED);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bookingService.cancel(bookingId, userId)
        );

        assertEquals(
                "Booking cannot be cancelled",
                exception.getMessage()
        );

        assertEquals(
                BookingStatus.COMPLETED,
                booking.getStatus()
        );
    }


    //----------GetById----------
    @Test
    void getById_shouldReturnBooking() {

        Long bookingId = 1L;
        Long userId = 2L;

        UserEntity user = new UserEntity();
        user.setId(userId);

        BookingEntity booking = new BookingEntity();
        booking.setId(bookingId);
        booking.setUser(user);

        BookingResponse response = new BookingResponse();
        response.setId(bookingId);
        response.setUserId(userId);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(bookingMapper.toResponse(booking))
                .thenReturn(response);

        BookingResponse result = bookingService.getById(
                bookingId,
                userId
        );

        assertNotNull(result);
        assertEquals(bookingId, result.getId());
        assertEquals(userId, result.getUserId());

        verify(bookingMapper).toResponse(booking);
    }

    @Test
    void getById_shouldThrowException_whenBookingBelongsToAnotherUser() {

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
                () -> bookingService.getById(
                        bookingId,
                        anotherUserId
                )
        );

        assertEquals(
                "You do not have access to this booking",
                exception.getMessage()
        );

        verifyNoInteractions(bookingMapper);
    }

    @Test
    void getById_shouldThrowException_whenBookingNotFound() {

        Long bookingId = 1L;
        Long userId = 2L;

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.getById(
                        bookingId,
                        userId
                )
        );

        assertEquals(
                "Booking with id " + bookingId + " not found",
                exception.getMessage()
        );

        verifyNoInteractions(bookingMapper);
    }

    //----------GetByUserId----------
    @Test
    void getByUserId_shouldReturnBookings() {

        Long userId = 1L;

        UserEntity user = new UserEntity();
        user.setId(userId);

        BookingEntity booking1 = new BookingEntity();
        booking1.setId(10L);
        booking1.setUser(user);

        BookingEntity booking2 = new BookingEntity();
        booking2.setId(20L);
        booking2.setUser(user);

        BookingResponse response1 = new BookingResponse();
        response1.setId(10L);

        BookingResponse response2 = new BookingResponse();
        response2.setId(20L);

        when(userRepository.existsById(userId))
                .thenReturn(true);

        when(bookingRepository.findByUserId(userId))
                .thenReturn(List.of(booking1, booking2));

        when(bookingMapper.toResponse(booking1))
                .thenReturn(response1);

        when(bookingMapper.toResponse(booking2))
                .thenReturn(response2);

        List<BookingResponse> result =
                bookingService.getByUserId(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).getId());
        assertEquals(20L, result.get(1).getId());

        verify(bookingRepository).findByUserId(userId);
    }

    @Test
    void getByUserId_shouldThrowException_whenUserNotFound() {

        Long userId = 1L;

        when(userRepository.existsById(userId))
                .thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.getByUserId(userId)
        );

        assertEquals(
                "User with id " + userId + " not found",
                exception.getMessage()
        );

        verifyNoInteractions(bookingRepository, bookingMapper);
    }
}
