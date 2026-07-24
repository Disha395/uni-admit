package com.example.payment_service.service;

import com.example.payment_service.config.RabbitMQConfig;
import com.example.payment_service.dto.PaymentInitiateRequest;
import com.example.payment_service.dto.PaymentResponse;
import com.example.payment_service.entity.PaymentTransaction;
import com.example.payment_service.event.PaymentEvent;
import com.example.payment_service.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RabbitTemplate rabbitTemplate;

    // ── Initiate payment — creates Stripe PaymentIntent ───────────────────────
    @Transactional
    public PaymentResponse initiatePayment(String studentId, PaymentInitiateRequest request) {
        String idempotencyKey = request.getApplicationId().toString() + "-" + studentId;

        // Idempotency check — return existing if already initiated
        return paymentRepository.findByIdempotencyKey(idempotencyKey)
                .map(existing -> {
                    log.info("Returning existing payment for idempotency key: {}", idempotencyKey);
                    return PaymentResponse.from(existing);
                })
                .orElseGet(() -> createNewPayment(studentId, request, idempotencyKey));
    }

    // ── Get payment by transaction ID ─────────────────────────────────────────
    public PaymentResponse getPayment(String transactionId) {
        PaymentTransaction txn = paymentRepository.findById(UUID.fromString(transactionId))
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
        return PaymentResponse.from(txn);
    }

    // ── Get all payments for an application ──────────────────────────────────
    public List<PaymentResponse> getPaymentsByApplication(String applicationId) {
        return paymentRepository.findByApplicationId(UUID.fromString(applicationId))
                .stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
    }

    // ── Handle Stripe webhook — payment.intent.succeeded ─────────────────────
    @Transactional
    public void handlePaymentSuccess(String paymentIntentId) {
        paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .ifPresent(txn -> {
                    txn.setStatus(PaymentTransaction.PaymentStatus.COMPLETED);
                    txn.setCompletedAt(Instant.now());
                    paymentRepository.save(txn);

                    // Publish payment.completed → Admission Service advances state
                    publishPaymentEvent(txn, "COMPLETED");
                    log.info("Payment completed for applicationId: {}", txn.getApplicationId());
                });
    }
    // ── Handle Stripe webhook — payment.intent.payment_failed ────────────────
    @Transactional
    public void handlePaymentFailure(String paymentIntentId, String reason) {
        paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .ifPresent(txn -> {
                    txn.setStatus(PaymentTransaction.PaymentStatus.FAILED);
                    txn.setFailureReason(reason);
                    paymentRepository.save(txn);

                    publishPaymentEvent(txn, "FAILED");
                    log.warn("Payment failed for applicationId: {}, reason: {}",
                            txn.getApplicationId(), reason);
                });
    }

    // ── Private helpers ───────────────────────────────────────────────────────
    private PaymentResponse createNewPayment(String studentId,
                                             PaymentInitiateRequest request, String idempotencyKey) {
        try {
            // Convert rupees to paise (Stripe uses smallest currency unit)
            long amountInPaise = request.getAmountInRupees() * 100;

            // Create Stripe PaymentIntent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInPaise)
                    .setCurrency(request.getCurrency())
                    .addPaymentMethodType("card")
                    .putMetadata("applicationId", request.getApplicationId().toString())
                    .putMetadata("studentId", studentId)
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            // Save to DB
            PaymentTransaction txn = PaymentTransaction.builder()
                    .idempotencyKey(idempotencyKey)
                    .applicationId(request.getApplicationId())
                    .studentId(UUID.fromString(studentId))
                    .stripePaymentIntentId(intent.getId())
                    .stripeClientSecret(intent.getClientSecret())
                    .amount(amountInPaise)
                    .currency(request.getCurrency())
                    .status(PaymentTransaction.PaymentStatus.PENDING)
                    .build();

            paymentRepository.save(txn);
            log.info("Payment initiated: {} for applicationId: {}",
                    intent.getId(), request.getApplicationId());

            return PaymentResponse.from(txn);

        } catch (StripeException e) {
            log.error("Stripe error: {}", e.getMessage());
            throw new RuntimeException("Payment initiation failed: " + e.getMessage());
        }
    }

    private void publishPaymentEvent(PaymentTransaction txn, String status) {
        PaymentEvent event = PaymentEvent.builder()
                .transactionId(txn.getId())
                .applicationId(txn.getApplicationId())
                .studentId(txn.getStudentId())
                .status(status)
                .amount(txn.getAmount())
                .currency(txn.getCurrency())
                .timestamp(Instant.now())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ADMISSION_EXCHANGE,
                RabbitMQConfig.PAYMENT_COMPLETED_KEY,
                event);
    }
}


