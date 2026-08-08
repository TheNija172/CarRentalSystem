package org.example.carrentalsystem.repository;

import org.example.carrentalsystem.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
