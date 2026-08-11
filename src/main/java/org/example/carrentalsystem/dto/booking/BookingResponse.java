package org.example.carrentalsystem.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.carrentalsystem.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {

    private Long id;

    private Long userId;

    private Long carId;

    private String carBrand;

    private String carModel;

    private Long pickupLocationId;

    private String pickupLocationName;

    private Long returnLocationId;

    private String returnLocationName;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal totalPrice;

    private BookingStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
