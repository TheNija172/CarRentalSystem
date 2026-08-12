package org.example.carrentalsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carrentalsystem.dto.booking.BookingCreateRequest;
import org.example.carrentalsystem.dto.booking.BookingResponse;
import org.example.carrentalsystem.entity.RoleEntity;
import org.example.carrentalsystem.entity.UserEntity;
import org.example.carrentalsystem.enums.BookingStatus;
import org.example.carrentalsystem.exception.BusinessException;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.security.CustomUserDetails;
import org.example.carrentalsystem.security.CustomUserDetailsService;
import org.example.carrentalsystem.security.jwt.JwtService;
import org.example.carrentalsystem.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@EnableMethodSecurity
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private BookingCreateRequest createRequest;
    private BookingResponse bookingResponse;

    @BeforeEach
    void setUp() {
        createRequest = new BookingCreateRequest();
        createRequest.setCarId(1L);
        createRequest.setPickupLocationId(1L);
        createRequest.setReturnLocationId(2L);
        createRequest.setStartDate(LocalDate.of(2026, 8, 15));
        createRequest.setEndDate(LocalDate.of(2026, 8, 20));

        bookingResponse = new BookingResponse();
        bookingResponse.setId(1L);
        bookingResponse.setUserId(1L);
        bookingResponse.setCarId(1L);
        bookingResponse.setCarBrand("Toyota");
        bookingResponse.setCarModel("Camry");
        bookingResponse.setPickupLocationId(1L);
        bookingResponse.setPickupLocationName("Downtown Office");
        bookingResponse.setReturnLocationId(2L);
        bookingResponse.setReturnLocationName("Airport Office");
        bookingResponse.setStartDate(LocalDate.of(2026, 8, 15));
        bookingResponse.setEndDate(LocalDate.of(2026, 8, 20));
        bookingResponse.setTotalPrice(new BigDecimal("250.00"));
        bookingResponse.setStatus(BookingStatus.CONFIRMED);
        bookingResponse.setCreatedAt(LocalDateTime.now());
        bookingResponse.setUpdatedAt(LocalDateTime.now());
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
        when(bookingService.create(any(BookingCreateRequest.class), eq(1L)))
                .thenReturn(bookingResponse);

        mockMvc.perform(post("/api/v1/bookings")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.carBrand").value("Toyota"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(bookingService).create(any(BookingCreateRequest.class), eq(1L));
    }

    @Test
    void create_shouldReturn400_whenRequestIsInvalid() throws Exception {
        Authentication auth = createAuth(1L, "USER");
        BookingCreateRequest invalidRequest = new BookingCreateRequest();

        mockMvc.perform(post("/api/v1/bookings")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).create(any(), any());
    }

    //----------GetMyBookings----------

    @Test
    void getMyBookings_shouldReturn200() throws Exception {
        Authentication auth = createAuth(1L, "USER");
        when(bookingService.getByUserId(1L)).thenReturn(List.of(bookingResponse));

        mockMvc.perform(get("/api/v1/bookings/my")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].userId").value(1L));

        verify(bookingService).getByUserId(1L);
    }

    //----------GetById----------

    @Test
    void getById_shouldReturn200_whenUserIsAdmin() throws Exception {
        Authentication auth = createAuth(1L, "ADMIN");
        when(bookingService.getById(1L, 1L)).thenReturn(bookingResponse);

        mockMvc.perform(get("/api/v1/bookings/1")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(bookingService).getById(1L, 1L);
    }

    @Test
    void getById_shouldReturn403_whenUserIsNotAdmin() throws Exception {
        Authentication auth = createAuth(1L, "USER");

        mockMvc.perform(get("/api/v1/bookings/1")
                        .with(authentication(auth)))
                .andExpect(status().isForbidden());

        verify(bookingService, never()).getById(any(), any());
    }

    //----------GetAll----------

    @Test
    void getAll_shouldReturn200_whenUserIsAdmin() throws Exception {
        Authentication auth = createAuth(1L, "ADMIN");
        when(bookingService.getAll()).thenReturn(List.of(bookingResponse));

        mockMvc.perform(get("/api/v1/bookings")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(bookingService).getAll();
    }

    @Test
    void getAll_shouldReturn403_whenUserIsNotAdmin() throws Exception {
        Authentication auth = createAuth(1L, "USER");

        mockMvc.perform(get("/api/v1/bookings")
                        .with(authentication(auth)))
                .andExpect(status().isForbidden());

        verify(bookingService, never()).getAll();
    }

    //----------GetByUserId----------

    @Test
    void getByUserId_shouldReturn200_whenUserIsAdmin() throws Exception {
        Authentication auth = createAuth(1L, "ADMIN");
        when(bookingService.getByUserId(5L)).thenReturn(List.of(bookingResponse));

        mockMvc.perform(get("/api/v1/bookings/user/5")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(bookingService).getByUserId(5L);
    }

    @Test
    void getByUserId_shouldReturn403_whenUserIsNotAdmin() throws Exception {
        Authentication auth = createAuth(1L, "USER");

        mockMvc.perform(get("/api/v1/bookings/user/5")
                        .with(authentication(auth)))
                .andExpect(status().isForbidden());

        verify(bookingService, never()).getByUserId(any());
    }

    //----------Cancel----------

    @Test
    void cancel_shouldReturn204() throws Exception {
        Authentication auth = createAuth(1L, "USER");
        doNothing().when(bookingService).cancel(1L, 1L);

        mockMvc.perform(patch("/api/v1/bookings/1/cancel")
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(bookingService).cancel(1L, 1L);
    }

    @Test
    void cancel_shouldReturn404_whenBookingNotFound() throws Exception {
        Authentication auth = createAuth(1L, "USER");
        doThrow(new ResourceNotFoundException("Booking not found"))
                .when(bookingService).cancel(99L, 1L);

        mockMvc.perform(patch("/api/v1/bookings/99/cancel")
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(bookingService).cancel(99L, 1L);
    }

    @Test
    void cancel_shouldReturn409_whenBookingCannotBeCancelled() throws Exception {
        Authentication auth = createAuth(1L, "USER");
        doThrow(new BusinessException("Booking cannot be cancelled"))
                .when(bookingService).cancel(1L, 1L);

        mockMvc.perform(patch("/api/v1/bookings/1/cancel")
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isConflict());

        verify(bookingService).cancel(1L, 1L);
    }
}