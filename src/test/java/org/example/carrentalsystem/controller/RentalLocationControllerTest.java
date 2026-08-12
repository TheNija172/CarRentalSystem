package org.example.carrentalsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carrentalsystem.dto.location.RentalLocationCreateRequest;
import org.example.carrentalsystem.dto.location.RentalLocationResponse;
import org.example.carrentalsystem.dto.location.RentalLocationUpdateRequest;
import org.example.carrentalsystem.exception.BusinessException;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.security.CustomUserDetailsService;
import org.example.carrentalsystem.security.jwt.JwtService;
import org.example.carrentalsystem.service.RentalLocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
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

@WebMvcTest(RentalLocationController.class)
@EnableMethodSecurity
class RentalLocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RentalLocationService rentalLocationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private RentalLocationCreateRequest createRequest;
    private RentalLocationUpdateRequest updateRequest;
    private RentalLocationResponse locationResponse;

    @BeforeEach
    void setUp() {
        createRequest = new RentalLocationCreateRequest();
        createRequest.setName("Downtown Office");
        createRequest.setAddress("123 Main St");
        createRequest.setCity("New York");
        createRequest.setDescription("Central location");

        updateRequest = new RentalLocationUpdateRequest();
        updateRequest.setName("Downtown Office Updated");
        updateRequest.setAddress("123 Main St");
        updateRequest.setCity("New York");
        updateRequest.setDescription("Updated description");
        updateRequest.setActive(true);

        locationResponse = new RentalLocationResponse();
        locationResponse.setId(1L);
        locationResponse.setName("Downtown Office");
        locationResponse.setAddress("123 Main St");
        locationResponse.setCity("New York");
        locationResponse.setDescription("Central location");
        locationResponse.setActive(true);
        locationResponse.setCreatedAt(LocalDateTime.now());
        locationResponse.setUpdatedAt(LocalDateTime.now());
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
    void getById_shouldReturn200_whenLocationExists() throws Exception {
        Long locationId = 1L;
        Authentication auth = createAuth("USER");
        when(rentalLocationService.getById(locationId)).thenReturn(locationResponse);

        mockMvc.perform(get("/api/v1/rental-locations/{id}", locationId)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Downtown Office"));

        verify(rentalLocationService).getById(locationId);
    }

    @Test
    void getById_shouldReturn404_whenLocationNotFound() throws Exception {
        Long locationId = 99L;
        Authentication auth = createAuth("USER");
        when(rentalLocationService.getById(locationId))
                .thenThrow(new ResourceNotFoundException("Location not found"));

        mockMvc.perform(get("/api/v1/rental-locations/{id}", locationId)
                        .with(authentication(auth)))
                .andExpect(status().isNotFound());

        verify(rentalLocationService).getById(locationId);
    }

    //----------GetAll----------

    @Test
    void getAll_shouldReturn200() throws Exception {
        Authentication auth = createAuth("USER");
        when(rentalLocationService.getAll()).thenReturn(List.of(locationResponse));

        mockMvc.perform(get("/api/v1/rental-locations")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Downtown Office"));

        verify(rentalLocationService).getAll();
    }

    //----------Create----------

    @Test
    void create_shouldReturn201_whenUserIsAdmin() throws Exception {
        Authentication auth = createAuth("ADMIN");
        when(rentalLocationService.create(any(RentalLocationCreateRequest.class)))
                .thenReturn(locationResponse);

        mockMvc.perform(post("/api/v1/rental-locations")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Downtown Office"));

        verify(rentalLocationService).create(any(RentalLocationCreateRequest.class));
    }

    @Test
    void create_shouldReturn400_whenRequestIsInvalid() throws Exception {
        Authentication auth = createAuth("ADMIN");
        RentalLocationCreateRequest invalidRequest = new RentalLocationCreateRequest();

        mockMvc.perform(post("/api/v1/rental-locations")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(rentalLocationService, never()).create(any());
    }

    @Test
    void create_shouldReturn403_whenUserIsNotAdmin() throws Exception {
        Authentication auth = createAuth("USER");

        mockMvc.perform(post("/api/v1/rental-locations")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(rentalLocationService, never()).create(any());
    }

    //----------Update----------

    @Test
    void update_shouldReturn200_whenUserIsAdmin() throws Exception {
        Long locationId = 1L;
        Authentication auth = createAuth("ADMIN");
        when(rentalLocationService.update(eq(locationId), any(RentalLocationUpdateRequest.class)))
                .thenReturn(locationResponse);

        mockMvc.perform(put("/api/v1/rental-locations/{id}", locationId)
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(rentalLocationService).update(eq(locationId), any(RentalLocationUpdateRequest.class));
    }

    @Test
    void update_shouldReturn403_whenUserIsNotAdmin() throws Exception {
        Long locationId = 1L;
        Authentication auth = createAuth("USER");

        mockMvc.perform(put("/api/v1/rental-locations/{id}", locationId)
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        verify(rentalLocationService, never()).update(any(), any());
    }

    @Test
    void update_shouldReturn404_whenLocationNotFound() throws Exception {
        Long locationId = 99L;
        Authentication auth = createAuth("ADMIN");
        when(rentalLocationService.update(eq(locationId), any(RentalLocationUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Location not found"));

        mockMvc.perform(put("/api/v1/rental-locations/{id}", locationId)
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(rentalLocationService).update(eq(locationId), any(RentalLocationUpdateRequest.class));
    }

    //----------Delete----------

    @Test
    void delete_shouldReturn204_whenUserIsAdmin() throws Exception {
        Long locationId = 1L;
        Authentication auth = createAuth("ADMIN");
        doNothing().when(rentalLocationService).delete(locationId);

        mockMvc.perform(delete("/api/v1/rental-locations/{id}", locationId)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(rentalLocationService).delete(locationId);
    }

    @Test
    void delete_shouldReturn403_whenUserIsNotAdmin() throws Exception {
        Long locationId = 1L;
        Authentication auth = createAuth("USER");

        mockMvc.perform(delete("/api/v1/rental-locations/{id}", locationId)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(rentalLocationService, never()).delete(any());
    }

    @Test
    void delete_shouldReturn404_whenLocationNotFound() throws Exception {
        Long locationId = 99L;
        Authentication auth = createAuth("ADMIN");
        doThrow(new ResourceNotFoundException("Location not found"))
                .when(rentalLocationService).delete(locationId);

        mockMvc.perform(delete("/api/v1/rental-locations/{id}", locationId)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(rentalLocationService).delete(locationId);
    }

    @Test
    void delete_shouldReturn409_whenLocationIsInUse() throws Exception {
        Long locationId = 1L;
        Authentication auth = createAuth("ADMIN");
        doThrow(new BusinessException("Location is in use"))
                .when(rentalLocationService).delete(locationId);

        mockMvc.perform(delete("/api/v1/rental-locations/{id}", locationId)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isConflict());

        verify(rentalLocationService).delete(locationId);
    }
}
