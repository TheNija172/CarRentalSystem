package org.example.carrentalsystem.controller;

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

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@Valid @RequestBody BookingCreateRequest request,
                                  @AuthenticationPrincipal CustomUserDetails userDetails)
    {

        return bookingService.create(request, userDetails.getUser().getId());
    }

    @GetMapping("/{id}")
    public BookingResponse getById(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {

        return bookingService.getById(id, userDetails.getUser().getId());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<BookingResponse> getAll() {

        return bookingService.getAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public List<BookingResponse> getByUserId(@PathVariable Long userId) {

        return bookingService.getByUserId(userId);
    }

    @GetMapping("/my")
    public List<BookingResponse> getMyBookings(@AuthenticationPrincipal CustomUserDetails userDetails) {

        return bookingService.getByUserId(userDetails.getUser().getId());
    }

    @PatchMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {

        bookingService.cancel(id, userDetails.getUser().getId());
    }
}
