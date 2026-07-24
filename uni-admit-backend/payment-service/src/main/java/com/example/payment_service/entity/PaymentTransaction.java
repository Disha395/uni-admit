package com.example.payment_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Idempotency key — prevents duplicate charges
    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    // Links to admission_db — no FK, different DB
    @Column(nullable = false)
    private UUID applicationId;

    // Links to auth_db — no FK, different DB
    @Column(nullable = false)
    private UUID studentId;

    // Stripe payment intent ID
    @Column(unique = true)
    private String stripePaymentIntentId;

    // Stripe client secret — sent to frontend for payment confirmation
    private String stripeClientSecret;

    @Column(nullable = false)
    private Long amount;   // in smallest currency unit (paise for INR, cents for USD)

    @Column(nullable = false)
    private String currency;  // "inr" or "usd"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    private String failureReason;

    private String receiptUrl;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;
    private Instant completedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public enum PaymentStatus {
        PENDING,    // payment intent created, awaiting confirmation
        COMPLETED,  // payment successful
        FAILED,     // payment failed
        REFUNDED    // payment refunded
    }
}
