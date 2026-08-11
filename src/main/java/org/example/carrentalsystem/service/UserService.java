package org.example.carrentalsystem.service;

import org.example.carrentalsystem.dto.user.UserCreateRequest;
import org.example.carrentalsystem.dto.user.UserResponse;
import org.example.carrentalsystem.dto.user.UserUpdateRequest;

import java.util.List;

public interface UserService {

    UserResponse create(UserCreateRequest request);

    UserResponse getById(Long id);

    UserResponse getByUsername(String username);

    List<UserResponse> getAll();

    UserResponse update(Long id, UserUpdateRequest request);

    void delete(Long id);
}
