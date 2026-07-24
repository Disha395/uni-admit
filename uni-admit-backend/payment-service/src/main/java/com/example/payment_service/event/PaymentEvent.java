package com.example.payment_service.event;

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
public class PaymentEvent {

    private UUID transactionId;
    private UUID applicationId;
    private UUID studentId;
    private String status;       // COMPLETED or FAILED
    private Long amount;
    private String currency;
    private Instant timestamp;
}
