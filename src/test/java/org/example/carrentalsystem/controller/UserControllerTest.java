package org.example.carrentalsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carrentalsystem.dto.user.UserResponse;
import org.example.carrentalsystem.dto.user.UserUpdateRequest;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.security.CustomUserDetailsService;
import org.example.carrentalsystem.security.jwt.JwtService;
import org.example.carrentalsystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@EnableMethodSecurity
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private UserUpdateRequest updateRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Doe");
        updateRequest.setPhone("+0987654321");

        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setUsername("johndoe");
        userResponse.setFirstName("John");
        userResponse.setLastName("Doe");
        userResponse.setPhone("+1234567890");
        userResponse.setActive(true);
        userResponse.setRole("USER");
        userResponse.setCreatedAt(LocalDateTime.now());
        userResponse.setUpdatedAt(LocalDateTime.now());
    }

    private Authentication createAuth(String... roles) {
        UserDetails userDetails = User.withUsername("testuser")
                .password("password")
                .roles(roles)
                .build();

        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
    }

    //----------GetById----------

    @Test
    void getById_shouldReturn200_whenUserExists() throws Exception {
        Long userId = 1L;
        Authentication auth = createAuth("USER");
        when(userService.getById(userId)).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/users/{id}", userId)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("johndoe"));

        verify(userService).getById(userId);
    }

    @Test
    void getById_shouldReturn404_whenUserNotFound() throws Exception {
        Long userId = 99L;
        Authentication auth = createAuth("USER");
        when(userService.getById(userId))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/{id}", userId)
                        .with(authentication(auth)))
                .andExpect(status().isNotFound());

        verify(userService).getById(userId);
    }

    //----------GetAll----------

    @Test
    void getAll_shouldReturn200_whenUserIsAdmin() throws Exception {
        Authentication auth = createAuth("ADMIN");
        when(userService.getAll()).thenReturn(List.of(userResponse));

        mockMvc.perform(get("/api/v1/users")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].username").value("johndoe"));

        verify(userService).getAll();
    }

    @Test
    void getAll_shouldReturn403_whenUserIsNotAdmin() throws Exception {
        Authentication auth = createAuth("USER");

        mockMvc.perform(get("/api/v1/users")
                        .with(authentication(auth)))
                .andExpect(status().isForbidden());

        verify(userService, never()).getAll();
    }

    //----------GetByUsername----------

    @Test
    void getByUsername_shouldReturn200_whenUserExists() throws Exception {
        String username = "johndoe";
        Authentication auth = createAuth("USER");
        when(userService.getByUsername(username)).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/users/username/{username}", username)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe"));

        verify(userService).getByUsername(username);
    }

    @Test
    void getByUsername_shouldReturn404_whenUserNotFound() throws Exception {
        String username = "unknown";
        Authentication auth = createAuth("USER");
        when(userService.getByUsername(username))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/username/{username}", username)
                        .with(authentication(auth)))
                .andExpect(status().isNotFound());

        verify(userService).getByUsername(username);
    }

    //----------Update----------

    @Test
    void update_shouldReturn200_whenRequestIsValid() throws Exception {
        Long userId = 1L;
        Authentication auth = createAuth("USER");
        when(userService.update(eq(userId), any(UserUpdateRequest.class)))
                .thenReturn(userResponse);

        mockMvc.perform(put("/api/v1/users/{id}", userId)
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(userService).update(eq(userId), any(UserUpdateRequest.class));
    }

    @Test
    void update_shouldReturn404_whenUserNotFound() throws Exception {
        Long userId = 99L;
        Authentication auth = createAuth("USER");
        when(userService.update(eq(userId), any(UserUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(put("/api/v1/users/{id}", userId)
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(userService).update(eq(userId), any(UserUpdateRequest.class));
    }

    @Test
    void update_shouldReturn403_whenAccessDenied() throws Exception {
        Long userId = 1L;
        Authentication auth = createAuth("USER");
        when(userService.update(eq(userId), any(UserUpdateRequest.class)))
                .thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(put("/api/v1/users/{id}", userId)
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        verify(userService).update(eq(userId), any(UserUpdateRequest.class));
    }

    //----------Delete----------

    @Test
    void delete_shouldReturn204_whenRequestIsValid() throws Exception {
        Long userId = 1L;
        Authentication auth = createAuth("USER");
        doNothing().when(userService).delete(userId);

        mockMvc.perform(delete("/api/v1/users/{id}", userId)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).delete(userId);
    }

    @Test
    void delete_shouldReturn404_whenUserNotFound() throws Exception {
        Long userId = 99L;
        Authentication auth = createAuth("USER");
        doThrow(new ResourceNotFoundException("User not found"))
                .when(userService).delete(userId);

        mockMvc.perform(delete("/api/v1/users/{id}", userId)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(userService).delete(userId);
    }

    @Test
    void delete_shouldReturn403_whenAccessDenied() throws Exception {
        Long userId = 1L;
        Authentication auth = createAuth("USER");
        doThrow(new AccessDeniedException("Access denied"))
                .when(userService).delete(userId);

        mockMvc.perform(delete("/api/v1/users/{id}", userId)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(userService).delete(userId);
    }
}