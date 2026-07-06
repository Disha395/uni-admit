package com.example.admin_service.controller;

import com.example.admin_service.dto.AnalyticsResponse;
import com.example.admin_service.dto.ApplicationDetailResponse;
import com.example.admin_service.dto.ApplicationResponse;
import com.example.admin_service.dto.StatusUpdateRequest;
import com.example.admin_service.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Admin Service is working");
    }

    // Get all applications
    @GetMapping("/applications")
    public ResponseEntity<List<ApplicationResponse>> getAllApplications(
                @RequestHeader("X-User-Roles") String roles) {
        if (!roles.contains("ROLE_ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(adminService.getAllApplications());
    }

    // Get single application with student profile merged
    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<ApplicationDetailResponse> getApplicationDetail(
            @PathVariable String applicationId,
            @RequestHeader("X-User-Roles") String roles) {
        if (!roles.contains("ROLE_ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(adminService.getApplicationDetail(applicationId));
    }

    // Review application — update status
    @PatchMapping("/applications/{applicationId}/review")
    public ResponseEntity<ApplicationResponse> reviewApplication(
            @PathVariable String applicationId,
            @RequestHeader("X-User-Id") String adminId,
            @RequestHeader("X-User-Roles") String roles,
            @Valid @RequestBody StatusUpdateRequest request) {
        if (!roles.contains("ROLE_ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(
                adminService.reviewApplication(applicationId, request, adminId));
    }

    // Analytics
    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            @RequestHeader("X-User-Roles") String roles) {
        if (!roles.contains("ROLE_ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        List<ApplicationResponse> applications = adminService.getAllApplications();
        return ResponseEntity.ok(adminService.getAnalytics(applications));
    }
}

