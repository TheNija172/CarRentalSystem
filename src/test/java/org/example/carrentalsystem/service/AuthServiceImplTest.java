package org.example.carrentalsystem.service;

import org.example.carrentalsystem.dto.auth.AuthResponse;
import org.example.carrentalsystem.dto.auth.LoginRequest;
import org.example.carrentalsystem.dto.auth.RegisterRequest;
import org.example.carrentalsystem.dto.user.UserCreateRequest;
import org.example.carrentalsystem.dto.user.UserResponse;
import org.example.carrentalsystem.exception.BusinessException;
import org.example.carrentalsystem.security.CustomUserDetailsService;
import org.example.carrentalsystem.security.jwt.JwtService;
import org.example.carrentalsystem.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UserResponse userResponse;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("johndoe");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setPhone("+1234567890");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("johndoe");
        loginRequest.setPassword("password123");

        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setUsername("johndoe");
        userResponse.setFirstName("John");
        userResponse.setLastName("Doe");
        userResponse.setPhone("+1234567890");
        userResponse.setRole("USER");
        userResponse.setActive(true);
        userResponse.setCreatedAt(LocalDateTime.now());
        userResponse.setUpdatedAt(LocalDateTime.now());

        userDetails = User.withUsername("johndoe")
                .password("password123")
                .roles("USER")
                .build();
    }

    //----------Register----------

    @Test
    void register_shouldRegisterUserSuccessfully() {
        when(userService.create(any(UserCreateRequest.class))).thenReturn(userResponse);
        when(userDetailsService.loadUserByUsername(userResponse.getUsername())).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        AuthResponse result = authService.register(registerRequest);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        verify(userService).create(any(UserCreateRequest.class));
        verify(userDetailsService).loadUserByUsername(userResponse.getUsername());
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void register_shouldPropagateException_whenUserServiceFails() {
        when(userService.create(any(UserCreateRequest.class)))
                .thenThrow(new BusinessException("Username is already taken"));

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Username is already taken");

        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtService, never()).generateToken(any());
    }

    //----------Login----------

    @Test
    void login_shouldLoginUserSuccessfully() {
        // Метод authenticate возвращает Authentication, поэтому используем when/thenReturn
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("johndoe", "password123"));

        when(userDetailsService.loadUserByUsername(loginRequest.getUsername())).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        AuthResponse result = authService.login(loginRequest);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService).loadUserByUsername(loginRequest.getUsername());
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void login_shouldThrowException_whenAuthenticationFails() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Bad credentials");

        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtService, never()).generateToken(any());
    }
}