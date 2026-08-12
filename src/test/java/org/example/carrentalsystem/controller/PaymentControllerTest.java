package org.example.carrentalsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carrentalsystem.dto.payment.PaymentCreateRequest;
import org.example.carrentalsystem.dto.payment.PaymentResponse;
import org.example.carrentalsystem.entity.RoleEntity;
import org.example.carrentalsystem.entity.UserEntity;
import org.example.carrentalsystem.enums.PaymentMethod;
import org.example.carrentalsystem.enums.PaymentStatus;
import org.example.carrentalsystem.exception.ResourceNotFoundException;
import org.example.carrentalsystem.security.CustomUserDetails;
import org.example.carrentalsystem.security.CustomUserDetailsService;
import org.example.carrentalsystem.security.jwt.JwtService;
import org.example.carrentalsystem.service.PaymentService;
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

@WebMvcTest(PaymentController.class)
@EnableMethodSecurity
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private PaymentCreateRequest createRequest;
    private PaymentResponse paymentResponse;

    @BeforeEach
    void setUp() {
        createRequest = new PaymentCreateRequest();
        createRequest.setPaymentMethod(PaymentMethod.CARD); // Подставь свое значение enum

        paymentResponse = new PaymentResponse();
        paymentResponse.setId(1L);
        paymentResponse.setBookingId(1L);
        paymentResponse.setAmount(new BigDecimal("100.00"));
        paymentResponse.setStatus(PaymentStatus.COMPLETED); // Подставь свое значение enum
        paymentResponse.setPaymentMethod(PaymentMethod.CARD);
        paymentResponse.setTransactionId("TXN-123");
        paymentResponse.setPaidAt(LocalDateTime.now());
        paymentResponse.setCreatedAt(LocalDateTime.now());
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
        Long bookingId = 1L;
        Authentication auth = createAuth(1L, "USER");
        when(paymentService.create(any(PaymentCreateRequest.class), eq(bookingId), eq(1L)))
                .thenReturn(paymentResponse);

        mockMvc.perform(post("/api/v1/payments/booking/{bookingId}", bookingId)
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.transactionId").value("TXN-123"));

        verify(paymentService).create(any(PaymentCreateRequest.class), eq(bookingId), eq(1L));
    }

    @Test
    void create_shouldReturn400_whenRequestIsInvalid() throws Exception {
        Long bookingId = 1L;
        Authentication auth = createAuth(1L, "USER");
        PaymentCreateRequest invalidRequest = new PaymentCreateRequest();

        mockMvc.perform(post("/api/v1/payments/booking/{bookingId}", bookingId)
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).create(any(), any(), any());
    }

    @Test
    void create_shouldReturn404_whenBookingNotFound() throws Exception {
        Long bookingId = 99L;
        Authentication auth = createAuth(1L, "USER");
        when(paymentService.create(any(PaymentCreateRequest.class), eq(bookingId), eq(1L)))
                .thenThrow(new ResourceNotFoundException("Booking not found"));

        mockMvc.perform(post("/api/v1/payments/booking/{bookingId}", bookingId)
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound());

        verify(paymentService).create(any(PaymentCreateRequest.class), eq(bookingId), eq(1L));
    }

    @Test
    void create_shouldReturn403_whenAccessDenied() throws Exception {
        Long bookingId = 1L;
        Authentication auth = createAuth(1L, "USER");
        when(paymentService.create(any(PaymentCreateRequest.class), eq(bookingId), eq(1L)))
                .thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(post("/api/v1/payments/booking/{bookingId}", bookingId)
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(paymentService).create(any(PaymentCreateRequest.class), eq(bookingId), eq(1L));
    }

    //----------GetById----------

    @Test
    void getById_shouldReturn200() throws Exception {
        Long paymentId = 1L;
        Authentication auth = createAuth(1L, "USER");
        when(paymentService.getById(paymentId, 1L)).thenReturn(paymentResponse);

        mockMvc.perform(get("/api/v1/payments/{id}", paymentId)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(paymentService).getById(paymentId, 1L);
    }

    @Test
    void getById_shouldReturn404_whenPaymentNotFound() throws Exception {
        Long paymentId = 99L;
        Authentication auth = createAuth(1L, "USER");
        when(paymentService.getById(paymentId, 1L))
                .thenThrow(new ResourceNotFoundException("Payment not found"));

        mockMvc.perform(get("/api/v1/payments/{id}", paymentId)
                        .with(authentication(auth)))
                .andExpect(status().isNotFound());

        verify(paymentService).getById(paymentId, 1L);
    }

    @Test
    void getById_shouldReturn403_whenAccessDenied() throws Exception {
        Long paymentId = 1L;
        Authentication auth = createAuth(2L, "USER");
        when(paymentService.getById(paymentId, 2L))
                .thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(get("/api/v1/payments/{id}", paymentId)
                        .with(authentication(auth)))
                .andExpect(status().isForbidden());

        verify(paymentService).getById(paymentId, 2L);
    }

    //----------GetByBookingId----------

    @Test
    void getByBookingId_shouldReturn200() throws Exception {
        Long bookingId = 1L;
        Authentication auth = createAuth(1L, "USER");
        when(paymentService.getByBookingId(bookingId, 1L)).thenReturn(List.of(paymentResponse));

        mockMvc.perform(get("/api/v1/payments/booking/{bookingId}", bookingId)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(paymentService).getByBookingId(bookingId, 1L);
    }

    @Test
    void getByBookingId_shouldReturn404_whenBookingNotFound() throws Exception {
        Long bookingId = 99L;
        Authentication auth = createAuth(1L, "USER");
        when(paymentService.getByBookingId(bookingId, 1L))
                .thenThrow(new ResourceNotFoundException("Booking not found"));

        mockMvc.perform(get("/api/v1/payments/booking/{bookingId}", bookingId)
                        .with(authentication(auth)))
                .andExpect(status().isNotFound());

        verify(paymentService).getByBookingId(bookingId, 1L);
    }

    @Test
    void getByBookingId_shouldReturn403_whenAccessDenied() throws Exception {
        Long bookingId = 1L;
        Authentication auth = createAuth(2L, "USER");
        when(paymentService.getByBookingId(bookingId, 2L))
                .thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(get("/api/v1/payments/booking/{bookingId}", bookingId)
                        .with(authentication(auth)))
                .andExpect(status().isForbidden());

        verify(paymentService).getByBookingId(bookingId, 2L);
    }

    //----------UpdateStatus----------

    @Test
    void updateStatus_shouldReturn200_whenUserIsAdmin() throws Exception {
        Long paymentId = 1L;
        Authentication auth = createAuth(1L, "ADMIN");
        PaymentStatus newStatus = PaymentStatus.COMPLETED;
        paymentResponse.setStatus(newStatus);

        when(paymentService.updateStatus(paymentId, newStatus)).thenReturn(paymentResponse);

        mockMvc.perform(patch("/api/v1/payments/{id}/status", paymentId)
                        .with(authentication(auth))
                        .with(csrf())
                        .param("status", newStatus.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(paymentService).updateStatus(paymentId, newStatus);
    }

    @Test
    void updateStatus_shouldReturn403_whenUserIsNotAdmin() throws Exception {
        Long paymentId = 1L;
        Authentication auth = createAuth(1L, "USER");

        mockMvc.perform(patch("/api/v1/payments/{id}/status", paymentId)
                        .with(authentication(auth))
                        .with(csrf())
                        .param("status", "COMPLETED"))
                .andExpect(status().isForbidden());

        verify(paymentService, never()).updateStatus(any(), any());
    }

    @Test
    void updateStatus_shouldReturn404_whenPaymentNotFound() throws Exception {
        Long paymentId = 99L;
        Authentication auth = createAuth(1L, "ADMIN");
        PaymentStatus newStatus = PaymentStatus.COMPLETED;

        when(paymentService.updateStatus(paymentId, newStatus))
                .thenThrow(new ResourceNotFoundException("Payment not found"));

        mockMvc.perform(patch("/api/v1/payments/{id}/status", paymentId)
                        .with(authentication(auth))
                        .with(csrf())
                        .param("status", newStatus.name()))
                .andExpect(status().isNotFound());

        verify(paymentService).updateStatus(paymentId, newStatus);
    }
}
