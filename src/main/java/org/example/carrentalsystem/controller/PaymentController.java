package org.example.carrentalsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carrentalsystem.dto.payment.PaymentCreateRequest;
import org.example.carrentalsystem.dto.payment.PaymentResponse;
import org.example.carrentalsystem.enums.PaymentStatus;
import org.example.carrentalsystem.security.CustomUserDetails;
import org.example.carrentalsystem.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Payments",
        description = "Operations for payment management"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(
            summary = "Create payment for booking",
            description = "Creates a new payment for the specified booking. Available to the booking owner"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid payment data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @PostMapping("/booking/{bookingId}")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse create(
            @PathVariable Long bookingId,
            @Valid @RequestBody PaymentCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        return paymentService.create(request, bookingId, userDetails.getUserEntity().getId());
    }

    @Operation(
            summary = "Get payment by ID",
            description = "Returns a payment by its ID. Available to the payment owner"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment successfully retrieved"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @GetMapping("/{id}")
    public PaymentResponse getById(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {

        return paymentService.getById(id, userDetails.getUserEntity().getId());
    }

    @Operation(
            summary = "Get payments by booking ID",
            description = "Returns all payments for the specified booking. Available to the booking owner"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payments successfully retrieved"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @GetMapping("/booking/{bookingId}")
    public List<PaymentResponse> getByBookingId(@PathVariable Long bookingId,
                                                @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        return paymentService.getByBookingId(bookingId, userDetails.getUserEntity().getId());
    }

    @Operation(
            summary = "Update payment status",
            description = "Updates the status of an existing payment. Available only to administrators"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment status successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public PaymentResponse updateStatus(@PathVariable Long id, @RequestParam PaymentStatus status) {

        return paymentService.updateStatus(id, status);
    }
}
