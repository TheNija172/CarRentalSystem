package org.example.carrentalsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carrentalsystem.dto.user.UserResponse;
import org.example.carrentalsystem.dto.user.UserUpdateRequest;
import org.example.carrentalsystem.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {

        return userService.getById(id);
    }

    @GetMapping
    public List<UserResponse> getAll() {

        return userService.getAll();
    }

    @GetMapping("/username/{username}")
    public UserResponse getByUsername(@PathVariable String username) {

        return userService.getByUsername(username);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {

        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        userService.delete(id);
    }
}
