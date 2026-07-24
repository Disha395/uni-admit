package com.example.payment_service.repository;

import com.example.payment_service.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentTransaction, UUID> {

    Optional<PaymentTransaction> findByIdempotencyKey(String idempotencyKey);

    Optional<PaymentTransaction> findByStripePaymentIntentId(String paymentIntentId);

    List<PaymentTransaction> findByApplicationId(UUID applicationId);

    List<PaymentTransaction> findByStudentId(UUID studentId);

    Optional<PaymentTransaction> findByApplicationIdAndStatus(
            UUID applicationId, PaymentTransaction.PaymentStatus status);
}
