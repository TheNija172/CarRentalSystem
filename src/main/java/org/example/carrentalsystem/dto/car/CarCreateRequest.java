package org.example.carrentalsystem.dto.car;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CarCreateRequest {

    @NotBlank
    private String brand;

    @NotBlank
    private String model;

    @NotNull
    private Integer productionYear;

    @NotBlank
    private String licensePlate;

    @NotBlank
    private String color;

    @NotNull
    private Transmission transmission;

    @NotNull
    private FuelType fuelType;

    @NotNull
    @Min(1)
    private Integer seats;

    @NotNull
    private Long categoryId;
}
