package org.example.carrentalsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carrentalsystem.dto.booking.BookingCreateRequest;
import org.example.carrentalsystem.dto.booking.BookingResponse;
import org.example.carrentalsystem.security.CustomUserDetails;
import org.example.carrentalsystem.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Bookings",
        description = "Operations for car bookings"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Operation(
            summary = "Create booking",
            description = "Creates a new booking for the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Booking successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid booking data"),
            @ApiResponse(responseCode = "409", description = "Car is already booked for the selected period")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@Valid @RequestBody BookingCreateRequest request,
                                  @AuthenticationPrincipal CustomUserDetails userDetails) {

        return bookingService.create(request, userDetails.getUserEntity().getId());
    }

    @Operation(summary = "Get my bookings",
            description = "Returns bookings of the authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "Bookings successfully retrieved")
    @GetMapping("/my")
    public List<BookingResponse> getMyBookings(@AuthenticationPrincipal CustomUserDetails userDetails) {

        return bookingService.getByUserId(userDetails.getUserEntity().getId());
    }

    @Operation(
            summary = "Get bookings by id",
            description = "Returns booking by id. Available only to administrators"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking successfully retrieved"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public BookingResponse getById(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {

        return bookingService.getById(id, userDetails.getUserEntity().getId());
    }


    @Operation(
            summary = "Get all bookings",
            description = "Returns all bookings. Available only to administrators"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bookings successfully retrieved"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<BookingResponse> getAll() {

        return bookingService.getAll();
    }

    @Operation(
            summary = "Get bookings by user id",
            description = "Returns all user bookings. Available only to administrators"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bookings successfully retrieved"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public List<BookingResponse> getByUserId(@PathVariable Long userId) {

        return bookingService.getByUserId(userId);
    }

    @Operation(
            summary = "Cancel booking",
            description = "Cancels a booking belonging to the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Booking successfully cancelled"),
            @ApiResponse(responseCode = "403", description = "User does not have access to this booking"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "409", description = "Booking cannot be cancelled")
    })
    @PatchMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {

        bookingService.cancel(id, userDetails.getUserEntity().getId());
    }
}
