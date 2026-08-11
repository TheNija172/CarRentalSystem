package org.example.carrentalsystem.service;

import org.example.carrentalsystem.dto.booking.BookingCreateRequest;
import org.example.carrentalsystem.dto.booking.BookingResponse;
import org.example.carrentalsystem.dto.booking.BookingUpdateRequest;

import java.util.List;

public interface BookingService {

    BookingResponse create(BookingCreateRequest request, Long userId);

    BookingResponse getById(Long id, Long userId);

    List<BookingResponse> getAll();

    List<BookingResponse> getByUserId(Long userId);

    void cancel(Long id, Long userId);
}
