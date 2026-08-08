package org.example.carrentalsystem.repository;

import org.example.carrentalsystem.entity.CarCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarCategoryRepository extends JpaRepository<CarCategory, Long> {
}
