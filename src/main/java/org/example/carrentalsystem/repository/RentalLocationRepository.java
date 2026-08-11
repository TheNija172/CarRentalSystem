package org.example.carrentalsystem.repository;

import org.example.carrentalsystem.entity.RentalLocation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RentalLocationRepository extends JpaRepository<RentalLocation, Long> {

    boolean existsByNameAndAddress(String name, String address);

    @EntityGraph(attributePaths = {})
    List<RentalLocation> findAllByActiveTrue();

    Optional<RentalLocation> findByIdAndActiveTrue(Long id);
}
