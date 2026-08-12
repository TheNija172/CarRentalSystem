package org.example.carrentalsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carrentalsystem.dto.review.ReviewCreateRequest;
import org.example.carrentalsystem.dto.review.ReviewResponse;
import org.example.carrentalsystem.entity.RoleEntity;
import org.example.carrentalsystem.entity.UserEntity;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.security.CustomUserDetails;
import org.example.carrentalsystem.security.CustomUserDetailsService;
import org.example.carrentalsystem.security.jwt.JwtService;
import org.example.carrentalsystem.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
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

@WebMvcTest(ReviewController.class)
@EnableMethodSecurity
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private ReviewCreateRequest createRequest;
    private ReviewResponse reviewResponse;

    @BeforeEach
    void setUp() {
        createRequest = new ReviewCreateRequest();
        createRequest.setRating(5);
        createRequest.setComment("Great car, highly recommend!");
        createRequest.setBookingId(1L);

        reviewResponse = new ReviewResponse();
        reviewResponse.setId(1L);
        reviewResponse.setUserId(1L);
        reviewResponse.setUsername("johndoe");
        reviewResponse.setCarId(1L);
        reviewResponse.setCarBrand("Toyota");
        reviewResponse.setCarModel("Camry");
        reviewResponse.setBookingId(1L);
        reviewResponse.setRating(5);
        reviewResponse.setComment("Great car, highly recommend!");
        reviewResponse.setCreatedAt(LocalDateTime.now());
        reviewResponse.setUpdatedAt(LocalDateTime.now());
    }

    private Authentication createAuth(Long userId, String roleName) {
        RoleEntity role = new RoleEntity();
        role.setId(1L);
        role.setName(roleName);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setUsername("testuser");
        userEntity.setPassword("password");
        userEntity.setFirstName("Test");
        userEntity.setLastName("User");
        userEntity.setRole(role);
        userEntity.setActive(true);

        CustomUserDetails userDetails = new CustomUserDetails(userEntity);

        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
    }

    //----------Create----------

    @Test
    void create_shouldReturn201_whenRequestIsValid() throws Exception {
        Authentication auth = createAuth(1L, "USER");
        when(reviewService.create(any(ReviewCreateRequest.class), eq(1L)))
                .thenReturn(reviewResponse);

        mockMvc.perform(post("/api/v1/reviews")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.carBrand").value("Toyota"));

        verify(reviewService).create(any(ReviewCreateRequest.class), eq(1L));
    }

    @Test
    void create_shouldReturn400_whenRequestIsInvalid() throws Exception {
        Authentication auth = createAuth(1L, "USER");
        ReviewCreateRequest invalidRequest = new ReviewCreateRequest();
        invalidRequest.setRating(0);

        mockMvc.perform(post("/api/v1/reviews")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).create(any(), any());
    }

    @Test
    void create_shouldReturn404_whenBookingNotFound() throws Exception {
        Authentication auth = createAuth(1L, "USER");
        when(reviewService.create(any(ReviewCreateRequest.class), eq(1L)))
                .thenThrow(new ResourceNotFoundException("Booking not found"));

        mockMvc.perform(post("/api/v1/reviews")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound());

        verify(reviewService).create(any(ReviewCreateRequest.class), eq(1L));
    }

    @Test
    void create_shouldReturn403_whenAccessDenied() throws Exception {
        Authentication auth = createAuth(1L, "USER");
        when(reviewService.create(any(ReviewCreateRequest.class), eq(1L)))
                .thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(post("/api/v1/reviews")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(reviewService).create(any(ReviewCreateRequest.class), eq(1L));
    }

    //----------GetById----------

    @Test
    void getById_shouldReturn200() throws Exception {
        Long reviewId = 1L;
        Authentication auth = createAuth(1L, "USER");
        when(reviewService.getById(reviewId)).thenReturn(reviewResponse);

        mockMvc.perform(get("/api/v1/reviews/{id}", reviewId)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("johndoe"));

        verify(reviewService).getById(reviewId);
    }

    @Test
    void getById_shouldReturn404_whenReviewNotFound() throws Exception {
        Long reviewId = 99L;
        Authentication auth = createAuth(1L, "USER");
        when(reviewService.getById(reviewId))
                .thenThrow(new ResourceNotFoundException("Review not found"));

        mockMvc.perform(get("/api/v1/reviews/{id}", reviewId)
                        .with(authentication(auth)))
                .andExpect(status().isNotFound());

        verify(reviewService).getById(reviewId);
    }

    //----------GetByCarId----------

    @Test
    void getByCarId_shouldReturn200() throws Exception {
        Long carId = 1L;
        Authentication auth = createAuth(1L, "USER");
        when(reviewService.getByCarId(carId)).thenReturn(List.of(reviewResponse));

        mockMvc.perform(get("/api/v1/reviews/car/{carId}", carId)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].carBrand").value("Toyota"));

        verify(reviewService).getByCarId(carId);
    }

    @Test
    void getByCarId_shouldReturn404_whenCarNotFound() throws Exception {
        Long carId = 99L;
        Authentication auth = createAuth(1L, "USER");
        when(reviewService.getByCarId(carId))
                .thenThrow(new ResourceNotFoundException("Car not found"));

        mockMvc.perform(get("/api/v1/reviews/car/{carId}", carId)
                        .with(authentication(auth)))
                .andExpect(status().isNotFound());

        verify(reviewService).getByCarId(carId);
    }

    //----------Delete----------

    @Test
    void delete_shouldReturn204() throws Exception {
        Long reviewId = 1L;
        Authentication auth = createAuth(1L, "USER");
        doNothing().when(reviewService).delete(reviewId, 1L);

        mockMvc.perform(delete("/api/v1/reviews/{id}", reviewId)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(reviewService).delete(reviewId, 1L);
    }

    @Test
    void delete_shouldReturn403_whenAccessDenied() throws Exception {
        Long reviewId = 1L;
        Authentication auth = createAuth(2L, "USER");
        doThrow(new AccessDeniedException("Access denied"))
                .when(reviewService).delete(reviewId, 2L);

        mockMvc.perform(delete("/api/v1/reviews/{id}", reviewId)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(reviewService).delete(reviewId, 2L);
    }

    @Test
    void delete_shouldReturn404_whenReviewNotFound() throws Exception {
        Long reviewId = 99L;
        Authentication auth = createAuth(1L, "USER");
        doThrow(new ResourceNotFoundException("Review not found"))
                .when(reviewService).delete(reviewId, 1L);

        mockMvc.perform(delete("/api/v1/reviews/{id}", reviewId)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(reviewService).delete(reviewId, 1L);
    }
}
