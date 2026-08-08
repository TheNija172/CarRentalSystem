package org.example.carrentalsystem.repository;

import org.example.carrentalsystem.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
