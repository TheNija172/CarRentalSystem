package org.example.carrentalsystem.service;

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
import org.example.carrentalsystem.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity userEntity;
    private RoleEntity roleEntity;
    private UserCreateRequest createRequest;
    private UserUpdateRequest updateRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        roleEntity = new RoleEntity();
        roleEntity.setId(1L);
        roleEntity.setName("USER");

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("johndoe");
        userEntity.setPassword("encodedPassword");
        userEntity.setFirstName("John");
        userEntity.setLastName("Doe");
        userEntity.setPhone("+1234567890");
        userEntity.setActive(true);
        userEntity.setRole(roleEntity);
        userEntity.setCreatedAt(LocalDateTime.now());
        userEntity.setUpdatedAt(LocalDateTime.now());

        createRequest = new UserCreateRequest();
        createRequest.setUsername("johndoe");
        createRequest.setPassword("password123");
        createRequest.setFirstName("John");
        createRequest.setLastName("Doe");
        createRequest.setPhone("+1234567890");

        updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Doe");
        updateRequest.setPhone("+0987654321");
        updateRequest.setPassword("newPassword");

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
    }

    //----------Create----------

    @Test
    void create_shouldCreateUserSuccessfully() {
        when(userRepository.existsByUsername(createRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByPhone(createRequest.getPhone())).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(roleEntity));
        when(userMapper.toEntity(createRequest)).thenReturn(userEntity);
        when(passwordEncoder.encode(createRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.toResponse(userEntity)).thenReturn(userResponse);

        UserResponse result = userService.create(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(userEntity.getUsername());
        verify(userRepository).existsByUsername(createRequest.getUsername());
        verify(userRepository).existsByPhone(createRequest.getPhone());
        verify(roleRepository).findByName("USER");
        verify(passwordEncoder).encode(createRequest.getPassword());
        verify(userRepository).save(userEntity);
    }

    @Test
    void create_shouldThrowException_whenUsernameAlreadyExists() {
        when(userRepository.existsByUsername(createRequest.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already taken");

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowException_whenPhoneAlreadyExists() {
        when(userRepository.existsByUsername(createRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByPhone(createRequest.getPhone())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowException_whenUserRoleNotFound() {
        when(userRepository.existsByUsername(createRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByPhone(createRequest.getPhone())).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Default USER role not found");

        verify(userRepository, never()).save(any());
    }

    //----------GetById----------

    @Test
    void getById_shouldReturnUser() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toResponse(userEntity)).thenReturn(userResponse);

        UserResponse result = userService.getById(userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
    }

    @Test
    void getById_shouldThrowException_whenUserNotFound() {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    //----------GetByUsername----------

    @Test
    void getByUsername_shouldReturnUser() {
        String username = "johndoe";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        when(userMapper.toResponse(userEntity)).thenReturn(userResponse);

        UserResponse result = userService.getByUsername(username);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
    }

    @Test
    void getByUsername_shouldThrowException_whenUserNotFound() {
        String username = "unknown";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByUsername(username))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    //----------GetAll----------

    @Test
    void getAll_shouldReturnUsers() {
        when(userRepository.findAll()).thenReturn(List.of(userEntity));
        when(userMapper.toResponse(userEntity)).thenReturn(userResponse);

        List<UserResponse> result = userService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(userRepository).findAll();
    }

    //----------Update----------

    @Test
    void update_shouldUpdateUserSuccessfully() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        doNothing().when(userMapper).updateEntity(updateRequest, userEntity);
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.toResponse(userEntity)).thenReturn(userResponse);

        UserResponse result = userService.update(userId, updateRequest);

        assertThat(result).isNotNull();
        verify(userMapper).updateEntity(updateRequest, userEntity);
        verify(userRepository).save(userEntity);
    }

    @Test
    void update_shouldThrowException_whenUserNotFound() {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(userId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    //----------Delete----------

    @Test
    void delete_shouldDeactivateUserSuccessfully() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        userService.delete(userId);

        assertThat(userEntity.isActive()).isFalse();
    }

    @Test
    void delete_shouldThrowException_whenUserNotFound() {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }
}