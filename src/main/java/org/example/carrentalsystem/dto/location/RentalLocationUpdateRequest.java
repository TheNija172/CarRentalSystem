package org.example.carrentalsystem.dto.location;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RentalLocationUpdateRequest {

    private String name;

    private String address;

    private String city;

    private String description;

    private Boolean active;
}
