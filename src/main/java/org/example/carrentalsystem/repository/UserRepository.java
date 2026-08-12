package org.example.carrentalsystem.repository;

import org.example.carrentalsystem.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByUsername(String username);

    Optional<UserEntity> findByUsername(String username);

    boolean existsByPhone(String phone);

    @EntityGraph(attributePaths = "role")
    Optional<UserEntity> findWithRoleByUsername(String username);
}
