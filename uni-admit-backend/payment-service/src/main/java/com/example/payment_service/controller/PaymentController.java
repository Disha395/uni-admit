package com.example.payment_service.controller;

import com.example.payment_service.dto.PaymentInitiateRequest;
import com.example.payment_service.dto.PaymentResponse;
import com.example.payment_service.service.PaymentService;
import com.example.payment_service.service.WebhookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final WebhookService webhookService;

    // Initiate payment — creates Stripe PaymentIntent, returns clientSecret to frontend
    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @RequestHeader("X-User-Id") String studentId,
            @Valid @RequestBody PaymentInitiateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.initiatePayment(studentId, request));
    }

    // Get payment by transaction ID
    @GetMapping("/{transactionId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String transactionId) {
        return ResponseEntity.ok(paymentService.getPayment(transactionId));
    }

    // Get all payments for an application
    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByApplication(
            @PathVariable String applicationId) {
        return ResponseEntity.ok(paymentService.getPaymentsByApplication(applicationId));
    }

    // Stripe webhook — receives payment success/failure events from Stripe
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            webhookService.processWebhook(payload, sigHeader);
            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            log.error("Webhook processing failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Webhook error: " + e.getMessage());
        }
    }
}
