package com.example.admin_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {

    @NotNull(message = "New status is required")
    private String newStatus;

    private String reason;

    private String adminComments;
}
