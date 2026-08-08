package org.example.carrentalsystem.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.carrentalsystem.enums.PaymentMethod;
import org.example.carrentalsystem.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {

    private Long id;

    private Long bookingId;

    private BigDecimal amount;

    private PaymentStatus status;

    private PaymentMethod paymentMethod;

    private String transactionId;

    private LocalDateTime paidAt;

    private LocalDateTime createdAt;

}
