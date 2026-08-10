package org.example.carrentalsystem.service;

import org.example.carrentalsystem.dto.review.ReviewCreateRequest;
import org.example.carrentalsystem.dto.review.ReviewResponse;

import java.util.List;

public interface ReviewService {

    ReviewResponse create(ReviewCreateRequest request, Long userId);

    ReviewResponse getById(Long id);

    List<ReviewResponse> getByCarId(Long carId);

    void delete(Long id);
}
