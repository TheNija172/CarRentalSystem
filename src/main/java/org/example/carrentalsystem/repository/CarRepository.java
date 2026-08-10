package org.example.carrentalsystem.repository;

import org.example.carrentalsystem.entity.Car;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {

    boolean existsByLicensePlate(String licensePlate);

    boolean existsByLicensePlateAndIdNot(String licensePlate, Long id);

    boolean existsByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = "category")
    List<Car> findAllByActiveTrue();

    @EntityGraph(attributePaths = "category")
    Optional<Car> findByIdAndActiveTrue(Long id);
}
