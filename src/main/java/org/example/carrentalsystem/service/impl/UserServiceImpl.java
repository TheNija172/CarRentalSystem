package org.example.carrentalsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalsystem.dto.user.UserCreateRequest;
import org.example.carrentalsystem.dto.user.UserResponse;
import org.example.carrentalsystem.dto.user.UserUpdateRequest;
import org.example.carrentalsystem.entity.RoleEntity;
import org.example.carrentalsystem.entity.UserEntity;
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

@Slf4j
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

        log.info("Creating user: name={}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {

            log.warn("Username is already taken: username={}", request.getUsername());

            throw new BusinessException(
                    "Username " + request.getUsername() + " is already taken"
            );
        }

        if (userRepository.existsByPhone(request.getPhone())) {

            log.warn("Phone is already taken: phone={}", request.getPhone());

            throw new BusinessException(
                    "Phone " + request.getPhone() + " is already registered"
            );
        }

        RoleEntity roleEntity = roleRepository.findByName("USER")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Default USER role not found"
                ));

        UserEntity userEntity = userMapper.toEntity(request);

        userEntity.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        userEntity.setRole(roleEntity);
        userEntity.setActive(true);

        UserEntity savedUser = userRepository.save(userEntity);

        log.info("User created successfully: username={}", request.getUsername());

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse getById(Long id) {

        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id " + id + " not found"
                ));

        return userMapper.toResponse(userEntity);
    }

    @Override
    public UserResponse getByUsername(String username) {

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with username "
                                + username
                                + " not found"
                ));

        return userMapper.toResponse(userEntity);
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

        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id " + id + " not found"
                ));

        userMapper.updateEntity(request, userEntity);

        UserEntity updatedUserEntity = userRepository.save(userEntity);

        return userMapper.toResponse(updatedUserEntity);
    }

    @Override
    @Transactional
    public void delete(Long id) {

        log.info("Deleting user: user id={}", id);

        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id " + id + " not found"
                ));

        userEntity.setActive(false);

        log.info("User deleted successfully: user id={}", id);
    }
}
