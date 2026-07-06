package com.example.admin_service.service;

import com.example.admin_service.client.AdmissionClient;
import com.example.admin_service.client.ProfileClient;
import com.example.admin_service.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AdmissionClient admissionClient;
    private final ProfileClient profileClient;

    public List<ApplicationResponse> getAllApplications() {
        log.info("Fetching all applications via Feign");
        List<ApplicationResponse> applications = admissionClient.getAllApplications();
        return applications != null ? applications : List.of();
    }


    // ── Get application with student profile merged ────────────────────────────
    public ApplicationDetailResponse getApplicationDetail(String applicationId) {
        ApplicationResponse application = admissionClient.getApplication(applicationId);

        if (application == null) {
            throw new IllegalArgumentException("Application not found: " + applicationId);
        }

        // Fetch student profile for enriched view
        ProfileResponse profile = null;
        if (application.getProfileId() != null) {
            profile = profileClient.getProfile(application.getProfileId().toString());
        }

        return buildDetailResponse(application, profile);
    }

    public ApplicationResponse reviewApplication(String applicationId,
                                                 StatusUpdateRequest request, String adminId) {

        Map<String, String> body = new HashMap<>();
        body.put("newStatus", request.getNewStatus());
        body.put("reason", request.getReason());
        body.put("adminComments", request.getAdminComments());

        ApplicationResponse updated = admissionClient.updateStatus(
                applicationId, adminId, "ROLE_ADMIN", body);

        if (updated == null) {
            throw new IllegalStateException(
                    "Failed to update — Admission Service unavailable");
        }
        return updated;
    }
    // ── Analytics ─────────────────────────────────────────────────────────────
    public AnalyticsResponse getAnalytics(List<ApplicationResponse> applications) {
        Map<String, Long> byStatus = applications.stream()
                .collect(Collectors.groupingBy(
                        ApplicationResponse::getStatus, Collectors.counting()));

        long uniqueStudents = applications.stream()
                .map(ApplicationResponse::getStudentId)
                .distinct()
                .count();

        return AnalyticsResponse.builder()
                .totalApplications(applications.size())
                .applicationsByStatus(byStatus)
                .totalStudents(uniqueStudents)
                .build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────
    private ApplicationDetailResponse buildDetailResponse(ApplicationResponse app,
                                                          ProfileResponse profile) {
        ApplicationDetailResponse.ApplicationDetailResponseBuilder builder =
                ApplicationDetailResponse.builder()
                        .applicationId(app.getId())
                        .courseName(app.getCourseName())
                        .university(app.getUniversity())
                        .intakeYear(app.getIntakeYear())
                        .status(app.getStatus())
                        .rejectionReason(app.getRejectionReason())
                        .adminComments(app.getAdminComments())
                        .submittedAt(app.getSubmittedAt());

        if (profile != null) {
            builder.studentFirstName(profile.getFirstName())
                    .studentLastName(profile.getLastName())
                    .studentPhone(profile.getPhone())
                    .studentCity(profile.getCity())
                    .tenthPercentage(profile.getTenthPercentage())
                    .twelfthPercentage(profile.getTwelfthPercentage());
        }

        return builder.build();
    }
}


