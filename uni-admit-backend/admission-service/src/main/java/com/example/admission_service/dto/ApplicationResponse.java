package com.example.admission_service.dto;

import com.example.admission_service.entity.Application;
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
public class ApplicationResponse {

    private UUID id;
    private UUID studentId;
    private UUID profileId;
    private String courseName;
    private String university;
    private Integer intakeYear;
    private String status;
    private String rejectionReason;
    private String adminComments;
    private Instant submittedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static ApplicationResponse from(Application app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .studentId(app.getStudentId())
                .profileId(app.getProfileId())
                .courseName(app.getCourseName())
                .university(app.getUniversity())
                .intakeYear(app.getIntakeYear())
                .status(app.getStatus().name())
                .rejectionReason(app.getRejectionReason())
                .adminComments(app.getAdminComments())
                .submittedAt(app.getSubmittedAt())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }
}
