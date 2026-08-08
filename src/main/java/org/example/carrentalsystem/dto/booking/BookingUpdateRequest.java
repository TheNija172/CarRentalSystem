package org.example.carrentalsystem.dto.booking;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.carrentalsystem.enums.BookingStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingUpdateRequest {

    @NotNull
    private BookingStatus status;
}
