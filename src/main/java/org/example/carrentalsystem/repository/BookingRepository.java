package org.example.carrentalsystem.repository;

import org.example.carrentalsystem.entity.Booking;
import org.example.carrentalsystem.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findByCarId(Long carId);

    @Query("""
        SELECT COUNT(b) > 0
        FROM Booking b
        WHERE b.car.id = :carId
          AND b.status <> :cancelledStatus
          AND b.startDate < :endDate
          AND b.endDate > :startDate
        """)
    boolean existsOverlappingBooking(
            @Param("carId") Long carId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("cancelledStatus") BookingStatus cancelledStatus
    );
}
