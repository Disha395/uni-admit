package com.example.admission_service.service;

import com.example.admission_service.config.RabbitMQConfig;
import com.example.admission_service.dto.ApplicationRequest;
import com.example.admission_service.dto.ApplicationResponse;
import com.example.admission_service.dto.StatusUpdateRequest;
import com.example.admission_service.entity.Application;
import com.example.admission_service.entity.StatusHistory;
import com.example.admission_service.event.ApplicationEvent;
import com.example.admission_service.repository.ApplicationRepository;
import com.example.admission_service.repository.StatusHistoryRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdmissionService {

    private final ApplicationRepository applicationRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final RabbitTemplate rabbitTemplate;

    // ── Submit application — returns 202 immediately, processes async ─────────
    @Transactional
    @Bulkhead(name = "admissionSubmit", fallbackMethod = "submitFallback")
    public ApplicationResponse submitApplication(String studentId, ApplicationRequest request) {
        UUID studentUUID = UUID.fromString(studentId);

        // Prevent duplicate active applications
        List<Application.ApplicationStatus> terminalStatuses = List.of(
                Application.ApplicationStatus.ACCEPTED,
                Application.ApplicationStatus.REJECTED
        );
        if (applicationRepository.existsByStudentIdAndStatusNotIn(studentUUID, terminalStatuses)) {
            throw new IllegalStateException("Student already has an active application");
        }

        Application application = Application.builder()
                .studentId(studentUUID)
                .profileId(request.getProfileId())
                .courseName(request.getCourseName())
                .university(request.getUniversity())
                .intakeYear(request.getIntakeYear())
                .status(Application.ApplicationStatus.SUBMITTED)
                .submittedAt(Instant.now())
                .build();

        applicationRepository.save(application);

        // Record status history
        recordStatusChange(application.getId(), null,
                Application.ApplicationStatus.SUBMITTED, studentId, "Application submitted");

        // Publish event to RabbitMQ — async processing by Admin + Notification services
        publishEvent(application, "APPLICATION_SUBMITTED", null,
                Application.ApplicationStatus.SUBMITTED);

        log.info("Application submitted: {} for student: {}", application.getId(), studentId);
        return ApplicationResponse.from(application);
    }

    // ── Bulkhead fallback — too many concurrent submissions ───────────────────
    public ApplicationResponse submitFallback(String studentId,
                                              ApplicationRequest request, Throwable t) {
        log.warn("Bulkhead limit reached for admission submissions");
        throw new IllegalStateException(
                "System is under high load. Please try again in a moment.");
    }

    // ── Get application by ID ─────────────────────────────────────────────────
    public ApplicationResponse getApplication(String applicationId) {
        Application application = findById(applicationId);
        return ApplicationResponse.from(application);
    }

    // ── Get all applications for a student ────────────────────────────────────
    public List<ApplicationResponse> getStudentApplications(String studentId) {
        return applicationRepository.findByStudentId(UUID.fromString(studentId))
                .stream()
                .map(ApplicationResponse::from)
                .collect(Collectors.toList());
    }

    // ── Update status — used by Admin Service ────────────────────────────────
    @Transactional
    public ApplicationResponse updateStatus(String applicationId,
                                            StatusUpdateRequest request, String adminId) {
        Application application = findById(applicationId);
        Application.ApplicationStatus oldStatus = application.getStatus();

        // Validate transition using state machine
        if (!application.canTransitionTo(request.getNewStatus())) {
            throw new IllegalStateException(
                    "Invalid transition: " + oldStatus + " → " + request.getNewStatus());
        }

        application.setStatus(request.getNewStatus());
        application.setAdminComments(request.getAdminComments());
        application.setReviewedBy(UUID.fromString(adminId));
        application.setReviewedAt(Instant.now());

        if (request.getNewStatus() == Application.ApplicationStatus.REJECTED) {
            application.setRejectionReason(request.getReason());
        }

        applicationRepository.save(application);

        // Record history
        recordStatusChange(application.getId(), oldStatus,
                request.getNewStatus(), adminId, request.getReason());

        // Publish status change event
        publishEvent(application, "STATUS_CHANGED", oldStatus, request.getNewStatus());

        return ApplicationResponse.from(application);
    }

    // ── Private helpers ───────────────────────────────────────────────────────
    private Application findById(String applicationId) {
        return applicationRepository.findById(UUID.fromString(applicationId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Application not found: " + applicationId));
    }

    private void recordStatusChange(UUID applicationId,
                                    Application.ApplicationStatus from,
                                    Application.ApplicationStatus to,
                                    String changedBy, String reason) {
        StatusHistory history = StatusHistory.builder()
                .applicationId(applicationId)
                .fromStatus(from)
                .toStatus(to)
                .changedBy(changedBy)
                .reason(reason)
                .build();
        statusHistoryRepository.save(history);
    }

    private void publishEvent(Application application, String eventType,
                              Application.ApplicationStatus oldStatus,
                              Application.ApplicationStatus newStatus) {
        ApplicationEvent event = ApplicationEvent.builder()
                .applicationId(application.getId())
                .studentId(application.getStudentId())
                .eventType(eventType)
                .oldStatus(oldStatus != null ? oldStatus.name() : null)
                .newStatus(newStatus.name())
                .courseName(application.getCourseName())
                .timestamp(Instant.now())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ADMISSION_EXCHANGE,
                eventType.equals("APPLICATION_SUBMITTED")
                        ? RabbitMQConfig.APPLICATION_SUBMITTED_KEY
                        : RabbitMQConfig.STATUS_CHANGED_KEY,
                event);

        log.info("Published event: {} for application: {}", eventType, application.getId());
    }
}
