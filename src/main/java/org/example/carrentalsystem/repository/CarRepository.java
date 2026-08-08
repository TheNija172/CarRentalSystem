package org.example.carrentalsystem.repository;

import org.example.carrentalsystem.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long> {
}
