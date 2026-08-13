package com.example.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/admission")
    public Mono<ResponseEntity<Map<String, Object>>> admissionFallback() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Admission Service is temporarily unavailable",
                        "status", 503,
                        "message", "The system is under high load. Your request has been queued. Please try again in a moment.",
                        "retryAfter", 10
                )));
    }

    @RequestMapping("/admin")
    public Mono<ResponseEntity<Map<String, Object>>> adminFallback() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Admin Service is temporarily unavailable",
                        "status", 503,
                        "message", "Please try again in a moment.",
                        "retryAfter", 10
                )));
    }
}