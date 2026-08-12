package org.example.carrentalsystem.security;

import org.example.carrentalsystem.entity.RoleEntity;
import org.example.carrentalsystem.entity.UserEntity;
import org.example.carrentalsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private UserEntity userEntity;
    private RoleEntity roleEntity;

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
    }

    //----------LoadUserByUsername----------

    @Test
    void loadUserByUsername_shouldReturnCustomUserDetails_whenUserExists() {
        when(userRepository.findWithRoleByUsername("johndoe"))
                .thenReturn(Optional.of(userEntity));

        UserDetails result = customUserDetailsService.loadUserByUsername("johndoe");

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(CustomUserDetails.class);
        assertThat(result.getUsername()).isEqualTo("johndoe");
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");

        verify(userRepository).findWithRoleByUsername("johndoe");
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {
        when(userRepository.findWithRoleByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found: unknown");

        verify(userRepository).findWithRoleByUsername("unknown");
    }

    @Test
    void loadUserByUsername_shouldReturnDisabledUser_whenUserIsInactive() {
        userEntity.setActive(false);
        when(userRepository.findWithRoleByUsername("johndoe"))
                .thenReturn(Optional.of(userEntity));

        UserDetails result = customUserDetailsService.loadUserByUsername("johndoe");

        assertThat(result).isNotNull();
        assertThat(result.isEnabled()).isFalse();

        verify(userRepository).findWithRoleByUsername("johndoe");
    }

    @Test
    void loadUserByUsername_shouldReturnAdminAuthorities_whenRoleIsAdmin() {
        roleEntity.setName("ADMIN");
        when(userRepository.findWithRoleByUsername("johndoe"))
                .thenReturn(Optional.of(userEntity));

        UserDetails result = customUserDetailsService.loadUserByUsername("johndoe");

        assertThat(result).isNotNull();
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");

        verify(userRepository).findWithRoleByUsername("johndoe");
    }
}
