package org.example.carrentalsystem.repository;

import org.example.carrentalsystem.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
