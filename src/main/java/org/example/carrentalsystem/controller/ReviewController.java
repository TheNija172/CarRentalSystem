package org.example.carrentalsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(
        name = "Reviews",
        description = "Operations for review management"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(
            summary = "Create review",
            description = "Creates a new review for a car. Available to authenticated users"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Review successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid review data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Car or booking not found")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@Valid @RequestBody ReviewCreateRequest request,
                                 @AuthenticationPrincipal CustomUserDetails userDetails) {

        return reviewService.create(request, userDetails.getUserEntity().getId());
    }

    @Operation(
            summary = "Get review by ID",
            description = "Returns a review by its ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @GetMapping("/{id}")
    public ReviewResponse getById(@PathVariable Long id) {

        return reviewService.getById(id);
    }

    @Operation(
            summary = "Get reviews by car ID",
            description = "Returns all reviews for the specified car"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reviews successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Car not found")
    })
    @GetMapping("/car/{carId}")
    public List<ReviewResponse> getByCarId(@PathVariable Long carId) {

        return reviewService.getByCarId(carId);
    }

    @Operation(
            summary = "Delete review",
            description = "Deletes a review. Available to the review author or administrators"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Review successfully deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
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