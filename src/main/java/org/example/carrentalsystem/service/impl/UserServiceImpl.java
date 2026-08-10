package org.example.carrentalsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carrentalsystem.dto.user.UserCreateRequest;
import org.example.carrentalsystem.dto.user.UserResponse;
import org.example.carrentalsystem.dto.user.UserUpdateRequest;
import org.example.carrentalsystem.entity.Role;
import org.example.carrentalsystem.entity.User;
import org.example.carrentalsystem.exception.BusinessException;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.mapper.UserMapper;
import org.example.carrentalsystem.repository.RoleRepository;
import org.example.carrentalsystem.repository.UserRepository;
import org.example.carrentalsystem.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse create(UserCreateRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(
                    "Username " + request.getUsername() + " is already taken"
            );
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException(
                    "Phone " + request.getPhone() + " is already registered"
            );
        }

        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Default USER role not found"
                ));

        User user = userMapper.toEntity(request);

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(role);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse getById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id " + id + " not found"
                ));

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getByUsername(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with username "
                                + username
                                + " not found"
                ));

        return userMapper.toResponse(user);
    }

    @Override
    public List<UserResponse> getAll() {

        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse update(
            Long id,
            UserUpdateRequest request
    ) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id " + id + " not found"
                ));

        userMapper.updateEntity(request, user);

        User updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id " + id + " not found"
                ));

        user.setActive(false);
    }
}
