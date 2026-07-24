package com.example.payment_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PaymentInitiateRequest {

    @NotNull(message = "Application ID is required")
    private UUID applicationId;

    // Amount in rupees (we convert to paise internally)
    // Fixed fee for demo: 1000 INR application fee
    private Long amountInRupees = 1000L;

    private String currency = "inr";
}
