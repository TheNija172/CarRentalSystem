package org.example.carrentalsystem.repository;

import org.example.carrentalsystem.entity.CarCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarCategoryRepository extends JpaRepository<CarCategoryEntity, Long> {
}
