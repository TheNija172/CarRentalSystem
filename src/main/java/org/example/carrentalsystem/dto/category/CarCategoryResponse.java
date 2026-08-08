package org.example.carrentalsystem.dto.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CarCategoryResponse {

    private Long id;

    private String name;

    private String description;

    private BigDecimal pricePerDay;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
