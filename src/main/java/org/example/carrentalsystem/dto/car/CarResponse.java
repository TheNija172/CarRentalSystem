package org.example.carrentalsystem.dto.car;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.carrentalsystem.enums.FuelType;
import org.example.carrentalsystem.enums.Transmission;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CarResponse {

    private Long id;

    private String brand;

    private String model;

    private Integer productionYear;

    private String licensePlate;

    private String color;

    private Transmission transmission;

    private FuelType fuelType;

    private Integer seats;

    private Long categoryId;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
