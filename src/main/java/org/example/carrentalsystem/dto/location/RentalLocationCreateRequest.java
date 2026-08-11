package org.example.carrentalsystem.dto.location;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RentalLocationCreateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String address;

    @NotBlank
    private String city;

    private String description;
}
