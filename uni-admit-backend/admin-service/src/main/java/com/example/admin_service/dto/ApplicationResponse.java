package com.example.admin_service.dto;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

// Mirrors Admission Service ApplicationResponse — used for Feign deserialization
@Data
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
}
