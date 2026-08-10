package org.example.carrentalsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carrentalsystem.dto.booking.BookingCreateRequest;
import org.example.carrentalsystem.dto.booking.BookingResponse;
import org.example.carrentalsystem.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@Valid @RequestBody BookingCreateRequest request, @RequestParam Long userId) {

        return bookingService.create(request, userId);
    }

    @GetMapping("/{id}")
    public BookingResponse getById(@PathVariable Long id) {

        return bookingService.getById(id);
    }

    @GetMapping
    public List<BookingResponse> getAll() {

        return bookingService.getAll();
    }

    @GetMapping("/user/{userId}")
    public List<BookingResponse> getByUserId(@PathVariable Long userId) {

        return bookingService.getByUserId(userId);
    }

    @PatchMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id) {

        bookingService.cancel(id);
    }
}
