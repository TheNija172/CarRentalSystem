package org.example.carrentalsystem.dto.car;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.carrentalsystem.enums.FuelType;
import org.example.carrentalsystem.enums.Transmission;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarUpdateRequest {

    private String brand;

    private String model;

    private Integer productionYear;

    private String licensePlate;

    private String color;

    private Transmission transmission;

    private FuelType fuelType;

    @Min(1)
    private Integer seats;

    private Boolean active;

    private Long categoryId;
}
