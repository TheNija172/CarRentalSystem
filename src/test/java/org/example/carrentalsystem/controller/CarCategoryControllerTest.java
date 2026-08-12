package org.example.carrentalsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carrentalsystem.dto.category.CarCategoryCreateRequest;
import org.example.carrentalsystem.dto.category.CarCategoryResponse;
import org.example.carrentalsystem.dto.category.CarCategoryUpdateRequest;
import org.example.carrentalsystem.exception.CategoryInUseException;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.security.CustomUserDetailsService;
import org.example.carrentalsystem.security.jwt.JwtService;
import org.example.carrentalsystem.service.CarCategoryService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarCategoryController.class)
@EnableMethodSecurity
class CarCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CarCategoryService carCategoryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private CarCategoryCreateRequest createRequest;
    private CarCategoryUpdateRequest updateRequest;
    private CarCategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        createRequest = new CarCategoryCreateRequest();
        createRequest.setName("Economy");
        createRequest.setDescription("Economy class cars");
        createRequest.setPricePerDay(new BigDecimal("50.00"));

        updateRequest = new CarCategoryUpdateRequest();
        updateRequest.setName("Economy Updated");
        updateRequest.setDescription("Updated description");
        updateRequest.setPricePerDay(new BigDecimal("60.00"));

        categoryResponse = new CarCategoryResponse();
        categoryResponse.setId(1L);
        categoryResponse.setName("Economy");
        categoryResponse.setDescription("Economy class cars");
        categoryResponse.setPricePerDay(new BigDecimal("50.00"));
        categoryResponse.setCreatedAt(LocalDateTime.now());
        categoryResponse.setUpdatedAt(LocalDateTime.now());
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
    void getById_shouldReturn200_whenCategoryExists() throws Exception {
        Long categoryId = 1L;
        Authentication auth = createAuth("USER"); // Добавили аутентификацию
        when(carCategoryService.getById(categoryId)).thenReturn(categoryResponse);

        mockMvc.perform(get("/api/v1/car-categories/{id}", categoryId)
                        .with(authentication(auth))) // 👈 Передали токен
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Economy"));

        verify(carCategoryService).getById(categoryId);
    }

    @Test
    void getById_shouldReturn404_whenCategoryNotFound() throws Exception {
        Long categoryId = 99L;
        Authentication auth = createAuth("USER"); // Добавили аутентификацию
        when(carCategoryService.getById(categoryId))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        mockMvc.perform(get("/api/v1/car-categories/{id}", categoryId)
                        .with(authentication(auth))) // 👈 Передали токен
                .andExpect(status().isNotFound());

        verify(carCategoryService).getById(categoryId);
    }

    //----------GetAll----------

    @Test
    void getAll_shouldReturn200() throws Exception {
        Authentication auth = createAuth("USER"); // Добавили аутентификацию
        when(carCategoryService.getAll()).thenReturn(List.of(categoryResponse));

        mockMvc.perform(get("/api/v1/car-categories")
                        .with(authentication(auth))) // 👈 Передали токен
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Economy"));

        verify(carCategoryService).getAll();
    }

    //----------Create----------

    @Test
    void create_shouldReturn201_whenUserIsAdmin() throws Exception {
        Authentication auth = createAuth("ADMIN");
        when(carCategoryService.create(any(CarCategoryCreateRequest.class)))
                .thenReturn(categoryResponse);

        mockMvc.perform(post("/api/v1/car-categories")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Economy"));

        verify(carCategoryService).create(any(CarCategoryCreateRequest.class));
    }

    @Test
    void create_shouldReturn400_whenRequestIsInvalid() throws Exception {
        Authentication auth = createAuth("ADMIN");
        CarCategoryCreateRequest invalidRequest = new CarCategoryCreateRequest();

        mockMvc.perform(post("/api/v1/car-categories")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(carCategoryService, never()).create(any());
    }

    @Test
    void create_shouldReturn403_whenUserIsNotAdmin() throws Exception {
        Authentication auth = createAuth("USER");

        mockMvc.perform(post("/api/v1/car-categories")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(carCategoryService, never()).create(any());
    }

    //----------Update----------

    @Test
    void update_shouldReturn200_whenUserIsAdmin() throws Exception {
        Long categoryId = 1L;
        Authentication auth = createAuth("ADMIN");
        when(carCategoryService.update(eq(categoryId), any(CarCategoryUpdateRequest.class)))
                .thenReturn(categoryResponse);

        mockMvc.perform(put("/api/v1/car-categories/{id}", categoryId)
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(carCategoryService).update(eq(categoryId), any(CarCategoryUpdateRequest.class));
    }

    @Test
    void update_shouldReturn400_whenRequestIsInvalid() throws Exception {
        Long categoryId = 1L;
        Authentication auth = createAuth("ADMIN");
        CarCategoryUpdateRequest invalidRequest = new CarCategoryUpdateRequest();
        invalidRequest.setPricePerDay(new BigDecimal("0.00"));

        mockMvc.perform(put("/api/v1/car-categories/{id}", categoryId)
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(carCategoryService, never()).update(any(), any());
    }

    @Test
    void update_shouldReturn403_whenUserIsNotAdmin() throws Exception {
        Long categoryId = 1L;
        Authentication auth = createAuth("USER");

        mockMvc.perform(put("/api/v1/car-categories/{id}", categoryId)
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        verify(carCategoryService, never()).update(any(), any());
    }

    @Test
    void update_shouldReturn404_whenCategoryNotFound() throws Exception {
        Long categoryId = 99L;
        Authentication auth = createAuth("ADMIN");
        when(carCategoryService.update(eq(categoryId), any(CarCategoryUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        mockMvc.perform(put("/api/v1/car-categories/{id}", categoryId)
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(carCategoryService).update(eq(categoryId), any(CarCategoryUpdateRequest.class));
    }

    //----------Delete----------

    @Test
    void delete_shouldReturn204_whenUserIsAdmin() throws Exception {
        Long categoryId = 1L;
        Authentication auth = createAuth("ADMIN");
        doNothing().when(carCategoryService).delete(categoryId);

        mockMvc.perform(delete("/api/v1/car-categories/{id}", categoryId)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(carCategoryService).delete(categoryId);
    }

    @Test
    void delete_shouldReturn403_whenUserIsNotAdmin() throws Exception {
        Long categoryId = 1L;
        Authentication auth = createAuth("USER");

        mockMvc.perform(delete("/api/v1/car-categories/{id}", categoryId)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(carCategoryService, never()).delete(any());
    }

    @Test
    void delete_shouldReturn404_whenCategoryNotFound() throws Exception {
        Long categoryId = 99L;
        Authentication auth = createAuth("ADMIN");
        doThrow(new ResourceNotFoundException("Category not found"))
                .when(carCategoryService).delete(categoryId);

        mockMvc.perform(delete("/api/v1/car-categories/{id}", categoryId)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(carCategoryService).delete(categoryId);
    }

    @Test
    void delete_shouldReturn409_whenCategoryIsInUse() throws Exception {
        Long categoryId = 1L;
        Authentication auth = createAuth("ADMIN");
        doThrow(new CategoryInUseException("Category is used by cars"))
                .when(carCategoryService).delete(categoryId);

        mockMvc.perform(delete("/api/v1/car-categories/{id}", categoryId)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isConflict());

        verify(carCategoryService).delete(categoryId);
    }
}
