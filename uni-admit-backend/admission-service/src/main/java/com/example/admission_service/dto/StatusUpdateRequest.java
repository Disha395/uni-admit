package com.example.admission_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import com.example.admission_service.entity.Application;

@Data
public class StatusUpdateRequest {

    @NotNull(message = "New status is required")
    private Application.ApplicationStatus newStatus;

    private String reason;

    private String adminComments;
}
