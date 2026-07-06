package com.example.admission_service.controller;

import com.example.admission_service.dto.ApplicationRequest;
import com.example.admission_service.dto.ApplicationResponse;
import com.example.admission_service.dto.StatusUpdateRequest;
import com.example.admission_service.service.AdmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/application")
@RequiredArgsConstructor
public class AdmissionController {

    private final AdmissionService admissionService;

    // Submit — returns 202 Accepted (async processing via RabbitMQ)
    @PostMapping
    public ResponseEntity<ApplicationResponse> submit(
            @RequestHeader("X-User-Id") String studentId,
            @Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(admissionService.submitApplication(studentId, request));
    }

    // Get single application
    @GetMapping("/{applicationId}")
    public ResponseEntity<ApplicationResponse> getApplication(
            @PathVariable String applicationId) {
        return ResponseEntity.ok(admissionService.getApplication(applicationId));
    }

    // Get all applications for logged-in student
    @GetMapping("/my")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(
            @RequestHeader("X-User-Id") String studentId) {
        return ResponseEntity.ok(admissionService.getStudentApplications(studentId));
    }

    // Update status — called by Admin Service
    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable String applicationId,
            @RequestHeader("X-User-Id") String adminId,
            @RequestHeader("X-User-Roles") String roles,
            @Valid @RequestBody StatusUpdateRequest request) {

        if (!roles.contains("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(admissionService.updateStatus(applicationId, request, adminId));
    }
    // Get all applications — admin only, called by Admin Service via Feign
    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getAllApplications() {
        return ResponseEntity.ok(admissionService.getAllApplications());
    }

}

