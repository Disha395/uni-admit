package com.example.admin_service.client;

import com.example.admin_service.config.FeignConfig;
import com.example.admin_service.dto.ApplicationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//@FeignClient(name = "admission-service", fallback = AdmissionClientFallback.class)
@FeignClient(name = "admission-service",
        url = "http://localhost:8083",
        configuration = FeignConfig.class,
        fallback = AdmissionClientFallback.class)
public interface AdmissionClient {

    @GetMapping("/application/{applicationId}")
    ApplicationResponse getApplication(@PathVariable("applicationId") String applicationId);

    @GetMapping("/application")
    List<ApplicationResponse> getAllApplications();

    @GetMapping("/application/my")
    List<ApplicationResponse> getStudentApplications(
            @RequestHeader("X-User-Id") String studentId);

    @PatchMapping("/application/{applicationId}/status")
    ApplicationResponse updateStatus(
            @PathVariable("applicationId") String applicationId,
            @RequestHeader("X-User-Id") String adminId,
            @RequestHeader("X-User-Roles") String roles,
            @RequestBody Map<String, String> request);
}