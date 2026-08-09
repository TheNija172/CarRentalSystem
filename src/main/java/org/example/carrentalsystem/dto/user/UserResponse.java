package org.example.carrentalsystem.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.carrentalsystem.entity.Role;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private Long id;

    private String username;

    private String firstName;

    private String lastName;

    private String phone;

    private Boolean active;

    private String role;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
