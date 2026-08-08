package org.example.carrentalsystem.repository;

import org.example.carrentalsystem.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
