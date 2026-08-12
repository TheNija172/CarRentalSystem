package org.example.carrentalsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalsystem.dto.auth.AuthResponse;
import org.example.carrentalsystem.dto.auth.LoginRequest;
import org.example.carrentalsystem.dto.auth.RegisterRequest;
import org.example.carrentalsystem.dto.user.UserCreateRequest;
import org.example.carrentalsystem.dto.user.UserResponse;
import org.example.carrentalsystem.security.CustomUserDetailsService;
import org.example.carrentalsystem.security.jwt.JwtService;
import org.example.carrentalsystem.service.AuthService;
import org.example.carrentalsystem.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Override
    public AuthResponse register(RegisterRequest request) {

        log.info(
                "User registration attempt: username={}",
                request.getUsername()
        );

        UserResponse user = userService.create(mapToUserCreateRequest(request));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        String token = jwtService.generateToken(userDetails);

        log.info(
                "User registered successfully: userId={}, username={}",
                user.getId(),
                user.getUsername()
        );

        return new AuthResponse(token);
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        log.info(
                "Authentication attempt: username={}",
                request.getUsername()
        );

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        String token = jwtService.generateToken(userDetails);

        log.info(
                "User authenticated successfully: username={}",
                userDetails.getUsername()
        );

        return new AuthResponse(token);
    }

    private UserCreateRequest mapToUserCreateRequest(RegisterRequest request) {

        UserCreateRequest userRequest = new UserCreateRequest();

        userRequest.setUsername(request.getUsername());
        userRequest.setPassword(request.getPassword());
        userRequest.setFirstName(request.getFirstName());
        userRequest.setLastName(request.getLastName());
        userRequest.setPhone(request.getPhone());

        return userRequest;
    }
}
