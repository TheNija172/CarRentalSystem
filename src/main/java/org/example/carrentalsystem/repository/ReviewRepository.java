package org.example.carrentalsystem.repository;

import org.example.carrentalsystem.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByBookingId(Long bookingId);

    List<Review> findByCarId(Long carId);

    boolean existsByBookingId(Long bookingId);
}
