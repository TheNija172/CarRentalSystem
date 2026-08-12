package org.example.carrentalsystem.repository;

import org.example.carrentalsystem.entity.RentalLocationEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RentalLocationRepository extends JpaRepository<RentalLocationEntity, Long> {

    boolean existsByNameAndAddress(String name, String address);

    @EntityGraph(attributePaths = {})
    List<RentalLocationEntity> findAllByActiveTrue();

    Optional<RentalLocationEntity> findByIdAndActiveTrue(Long id);
}
