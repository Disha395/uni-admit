package com.example.admission_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ApplicationRequest {

    @NotNull(message = "Profile ID is required")
    private UUID profileId;

    @NotBlank(message = "Course name is required")
    private String courseName;

    private String university;

    private Integer intakeYear;
}
