package com.dropshipping.backend.repository;

import com.dropshipping.backend.entity.Payment;
import com.dropshipping.backend.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByStripePaymentIntentId(String intentId);
    Optional<Payment> findByStripeChargeId(String chargeId);
    List<Payment> findAllByStatus(PaymentStatus status);
    List<Payment> findAllByOrder_Id(Long orderId);
    List<Payment> findAllByStatusAndCreatedAtBetween(PaymentStatus status, LocalDateTime start, LocalDateTime end);
}
