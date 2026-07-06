package com.example.admin_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

// Aggregated response combining Application + Profile data
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDetailResponse {

    // Application fields
    private UUID applicationId;
    private String courseName;
    private String university;
    private Integer intakeYear;
    private String status;
    private String rejectionReason;
    private String adminComments;
    private Instant submittedAt;

    // Profile fields
    private String studentFirstName;
    private String studentLastName;
    private String studentPhone;
    private String studentCity;
    private Double tenthPercentage;
    private Double twelfthPercentage;
}
