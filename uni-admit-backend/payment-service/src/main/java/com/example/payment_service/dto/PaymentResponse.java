package com.example.payment_service.dto;

import com.example.payment_service.entity.PaymentTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private UUID transactionId;
    private UUID applicationId;
    private UUID studentId;
    private String stripePaymentIntentId;
    private String clientSecret;       // sent to frontend for Stripe.js confirmation
    private Long amount;
    private String currency;
    private String status;
    private String receiptUrl;
    private Instant createdAt;
    private Instant completedAt;

    public static PaymentResponse from(PaymentTransaction txn) {
        return PaymentResponse.builder()
                .transactionId(txn.getId())
                .applicationId(txn.getApplicationId())
                .studentId(txn.getStudentId())
                .stripePaymentIntentId(txn.getStripePaymentIntentId())
                .clientSecret(txn.getStripeClientSecret())
                .amount(txn.getAmount())
                .currency(txn.getCurrency())
                .status(txn.getStatus().name())
                .receiptUrl(txn.getReceiptUrl())
                .createdAt(txn.getCreatedAt())
                .completedAt(txn.getCompletedAt())
                .build();
    }
}


