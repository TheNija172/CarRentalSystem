package org.example.carrentalsystem.repository;

import org.example.carrentalsystem.entity.RentalLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalLocationRepository extends JpaRepository<RentalLocation, Long> {
}
