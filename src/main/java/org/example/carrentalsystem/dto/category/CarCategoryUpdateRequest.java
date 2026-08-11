package org.example.carrentalsystem.dto.category;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CarCategoryUpdateRequest {

    private String name;

    private String description;

    @DecimalMin(value = "0.01")
    private BigDecimal pricePerDay;
}
