package org.example.carrentalsystem.repository;

import org.example.carrentalsystem.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    Optional<ReviewEntity> findByBookingId(Long bookingId);

    List<ReviewEntity> findByCarId(Long carId);

    boolean existsByBookingId(Long bookingId);
}
