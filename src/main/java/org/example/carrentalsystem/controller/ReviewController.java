package org.example.carrentalsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carrentalsystem.dto.review.ReviewCreateRequest;
import org.example.carrentalsystem.dto.review.ReviewResponse;
import org.example.carrentalsystem.security.CustomUserDetails;
import org.example.carrentalsystem.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@Valid @RequestBody ReviewCreateRequest request,
                                 @AuthenticationPrincipal CustomUserDetails userDetails)
    {

        return reviewService.create(request, userDetails.getUserEntity().getId());
    }

    @GetMapping("/{id}")
    public ReviewResponse getById(@PathVariable Long id) {

        return reviewService.getById(id);
    }

    @GetMapping("/car/{carId}")
    public List<ReviewResponse> getByCarId(@PathVariable Long carId) {

        return reviewService.getByCarId(carId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        reviewService.delete(
                id,
                userDetails.getUserEntity().getId()
        );
    }
}