package org.example.carrentalsystem.repository;

import org.example.carrentalsystem.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    boolean existsByTransactionId(String transactionId);

    Optional<PaymentEntity> findByTransactionId(String transactionId);

    List<PaymentEntity> findByBookingId(Long bookingId);
}
