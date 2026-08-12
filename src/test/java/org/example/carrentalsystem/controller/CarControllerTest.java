package org.example.carrentalsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carrentalsystem.dto.car.CarCreateRequest;
import org.example.carrentalsystem.dto.car.CarResponse;
import org.example.carrentalsystem.dto.car.CarUpdateRequest;
import org.example.carrentalsystem.enums.FuelType;
import org.example.carrentalsystem.enums.Transmission;
import org.example.carrentalsystem.exception.CarAlreadyExistsException;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.security.CustomUserDetailsService;
import org.example.carrentalsystem.security.jwt.JwtService;
import org.example.carrentalsystem.service.CarService;
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

@WebMvcTest(CarController.class)
@EnableMethodSecurity
class CarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CarService carService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private CarCreateRequest createRequest;
    private CarUpdateRequest updateRequest;
    private CarResponse carResponse;

    @BeforeEach
    void setUp() {
        createRequest = new CarCreateRequest();
        createRequest.setBrand("Toyota");
        createRequest.setModel("Camry");
        createRequest.setProductionYear(2022);
        createRequest.setLicensePlate("A123BC");
        createRequest.setColor("Black");
        createRequest.setTransmission(Transmission.AUTOMATIC);
        createRequest.setFuelType(FuelType.PETROL);
        createRequest.setSeats(5);
        createRequest.setCategoryId(1L);

        updateRequest = new CarUpdateRequest();
        updateRequest.setBrand("Toyota");
        updateRequest.setModel("Camry");
        updateRequest.setProductionYear(2022);
        updateRequest.setLicensePlate("A123BC");
        updateRequest.setColor("Black");
        updateRequest.setTransmission(Transmission.AUTOMATIC);
        updateRequest.setFuelType(FuelType.PETROL);
        updateRequest.setSeats(5);
        updateRequest.setActive(true);
        updateRequest.setCategoryId(1L);

        carResponse = new CarResponse();
        carResponse.setId(1L);
        carResponse.setBrand("Toyota");
        carResponse.setModel("Camry");
        carResponse.setProductionYear(2022);
        carResponse.setLicensePlate("A123BC");
        carResponse.setColor("Black");
        carResponse.setTransmission(Transmission.AUTOMATIC);
        carResponse.setFuelType(FuelType.PETROL);
        carResponse.setSeats(5);
        carResponse.setCategoryId(1L);
        carResponse.setCategoryName("Economy");
        carResponse.setActive(true);
        carResponse.setCreatedAt(LocalDateTime.now());
        carResponse.setUpdatedAt(LocalDateTime.now());
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
    void getById_shouldReturn200_whenCarExists() throws Exception {
        Long carId = 1L;
        Authentication auth = createAuth("USER");
        when(carService.getById(carId)).thenReturn(carResponse);

        mockMvc.perform(get("/api/v1/cars/{id}", carId)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.brand").value("Toyota"));

        verify(carService).getById(carId);
    }

    @Test
    void getById_shouldReturn404_whenCarNotFound() throws Exception {
        Long carId = 99L;
        Authentication auth = createAuth("USER");
        when(carService.getById(carId))
                .thenThrow(new ResourceNotFoundException("Car not found"));

        mockMvc.perform(get("/api/v1/cars/{id}", carId)
                        .with(authentication(auth)))
                .andExpect(status().isNotFound());

        verify(carService).getById(carId);
    }

    //----------GetAll----------

    @Test
    void getAll_shouldReturn200() throws Exception {
        Authentication auth = createAuth("USER");
        when(carService.getAll()).thenReturn(List.of(carResponse));

        mockMvc.perform(get("/api/v1/cars")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].brand").value("Toyota"));

        verify(carService).getAll();
    }

    //----------Create----------

    @Test
    void create_shouldReturn201_whenUserIsAdmin() throws Exception {
        Authentication auth = createAuth("ADMIN");
        when(carService.create(any(CarCreateRequest.class)))
                .thenReturn(carResponse);

        mockMvc.perform(post("/api/v1/cars")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.brand").value("Toyota"));

        verify(carService).create(any(CarCreateRequest.class));
    }

    @Test
    void create_shouldReturn400_whenRequestIsInvalid() throws Exception {
        Authentication auth = createAuth("ADMIN");
        CarCreateRequest invalidRequest = new CarCreateRequest();

        mockMvc.perform(post("/api/v1/cars")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(carService, never()).create(any());
    }

    @Test
    void create_shouldReturn403_whenUserIsNotAdmin() throws Exception {
        Authentication auth = createAuth("USER");

        mockMvc.perform(post("/api/v1/cars")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(carService, never()).create(any());
    }

    @Test
    void create_shouldReturn409_whenLicensePlateAlreadyExists() throws Exception {
        Authentication auth = createAuth("ADMIN");
        when(carService.create(any(CarCreateRequest.class)))
                .thenThrow(new CarAlreadyExistsException("License plate already exists"));

        mockMvc.perform(post("/api/v1/cars")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict());

        verify(carService).create(any(CarCreateRequest.class));
    }

    //----------Update----------

    @Test
    void update_shouldReturn200_whenUserIsAdmin() throws Exception {
        Long carId = 1L;
        Authentication auth = createAuth("ADMIN");
        when(carService.update(eq(carId), any(CarUpdateRequest.class)))
                .thenReturn(carResponse);

        mockMvc.perform(put("/api/v1/cars/{id}", carId)
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(carService).update(eq(carId), any(CarUpdateRequest.class));
    }

    @Test
    void update_shouldReturn403_whenUserIsNotAdmin() throws Exception {
        Long carId = 1L;
        Authentication auth = createAuth("USER");

        mockMvc.perform(put("/api/v1/cars/{id}", carId)
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        verify(carService, never()).update(any(), any());
    }

    @Test
    void update_shouldReturn404_whenCarNotFound() throws Exception {
        Long carId = 99L;
        Authentication auth = createAuth("ADMIN");
        when(carService.update(eq(carId), any(CarUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Car not found"));

        mockMvc.perform(put("/api/v1/cars/{id}", carId)
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(carService).update(eq(carId), any(CarUpdateRequest.class));
    }

    //----------Delete----------

    @Test
    void delete_shouldReturn204_whenUserIsAdmin() throws Exception {
        Long carId = 1L;
        Authentication auth = createAuth("ADMIN");
        doNothing().when(carService).delete(carId);

        mockMvc.perform(delete("/api/v1/cars/{id}", carId)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(carService).delete(carId);
    }

    @Test
    void delete_shouldReturn403_whenUserIsNotAdmin() throws Exception {
        Long carId = 1L;
        Authentication auth = createAuth("USER");

        mockMvc.perform(delete("/api/v1/cars/{id}", carId)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(carService, never()).delete(any());
    }

    @Test
    void delete_shouldReturn404_whenCarNotFound() throws Exception {
        Long carId = 99L;
        Authentication auth = createAuth("ADMIN");
        doThrow(new ResourceNotFoundException("Car not found"))
                .when(carService).delete(carId);

        mockMvc.perform(delete("/api/v1/cars/{id}", carId)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(carService).delete(carId);
    }
}
