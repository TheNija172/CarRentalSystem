package org.example.carrentalsystem.security;

import org.example.carrentalsystem.entity.RoleEntity;
import org.example.carrentalsystem.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    private UserEntity userEntity;
    private RoleEntity roleEntity;
    private CustomUserDetails customUserDetails;

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

        customUserDetails = new CustomUserDetails(userEntity);
    }

    //----------Constructor----------

    @Test
    void constructor_shouldSetUserEntity() {
        assertThat(customUserDetails.getUserEntity()).isEqualTo(userEntity);
    }

    //----------GetAuthorities----------

    @Test
    void getAuthorities_shouldReturnRoleWithPrefix() {
        Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();

        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void getAuthorities_shouldReturnAdminRole_whenRoleIsAdmin() {
        roleEntity.setName("ADMIN");
        customUserDetails = new CustomUserDetails(userEntity);

        Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();

        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    //----------GetPassword----------

    @Test
    void getPassword_shouldDelegateToUserEntity() {
        assertThat(customUserDetails.getPassword()).isEqualTo("encodedPassword");
    }

    //----------GetUsername----------

    @Test
    void getUsername_shouldDelegateToUserEntity() {
        assertThat(customUserDetails.getUsername()).isEqualTo("johndoe");
    }

    //----------AccountStatus----------

    @Test
    void isAccountNonExpired_shouldReturnTrue() {
        assertThat(customUserDetails.isAccountNonExpired()).isTrue();
    }

    @Test
    void isAccountNonLocked_shouldReturnTrue() {
        assertThat(customUserDetails.isAccountNonLocked()).isTrue();
    }

    @Test
    void isCredentialsNonExpired_shouldReturnTrue() {
        assertThat(customUserDetails.isCredentialsNonExpired()).isTrue();
    }

    //----------IsEnabled----------

    @Test
    void isEnabled_shouldReturnTrue_whenUserIsActive() {
        userEntity.setActive(true);
        customUserDetails = new CustomUserDetails(userEntity);

        assertThat(customUserDetails.isEnabled()).isTrue();
    }

    @Test
    void isEnabled_shouldReturnFalse_whenUserIsInactive() {
        userEntity.setActive(false);
        customUserDetails = new CustomUserDetails(userEntity);

        assertThat(customUserDetails.isEnabled()).isFalse();
    }
}
