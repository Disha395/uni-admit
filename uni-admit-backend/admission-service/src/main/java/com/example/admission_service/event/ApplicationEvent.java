package com.example.admission_service.event;

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
public class ApplicationEvent {

    private UUID applicationId;
    private UUID studentId;
    private String eventType;       // APPLICATION_SUBMITTED, STATUS_CHANGED
    private String oldStatus;
    private String newStatus;
    private String courseName;
    private Instant timestamp;
}
