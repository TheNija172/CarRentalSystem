package org.example.carrentalsystem.repository;

import jakarta.persistence.LockModeType;
import org.example.carrentalsystem.entity.CarEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<CarEntity, Long> {

    boolean existsByLicensePlate(String licensePlate);

    boolean existsByLicensePlateAndIdNot(String licensePlate, Long id);

    boolean existsByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = "category")
    List<CarEntity> findAllByActiveTrue();

    @EntityGraph(attributePaths = "category")
    Optional<CarEntity> findByIdAndActiveTrue(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "category")
    @Query("""
            SELECT c
            FROM CarEntity c
            WHERE c.id = :id
              AND c.active = true
            """)
    Optional<CarEntity> findActiveCarForUpdate(Long id);
}
