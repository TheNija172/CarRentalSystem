package org.example.carrentalsystem.dto.booking;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingCreateRequest {

    @NotNull
    private Long carId;

    @NotNull
    private Long pickupLocationId;

    @NotNull
    private Long returnLocationId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}
