package com.example.notification_service.listener;

import com.example.notification_service.event.ApplicationEvent;
import com.example.notification_service.event.UserRegisteredEvent;
import com.example.notification_service.service.EmailService;
import com.example.notification_service.service.StudentLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final EmailService emailService;
    private final StudentLookupService studentLookupService;

    // ── Listen: application submitted ────────────────────────────────────────
    @RabbitListener(queues = "q.application.submitted")
    public void onApplicationSubmitted(ApplicationEvent event) {
        log.info("Received APPLICATION_SUBMITTED event for application: {}",
                event.getApplicationId());

        try {
            // Look up student email from Auth Service
            String studentEmail = studentLookupService.getStudentEmail(
                    event.getStudentId().toString());

            Context context = new Context();
            context.setVariable("applicationId", event.getApplicationId().toString());
            context.setVariable("courseName", event.getCourseName());
            context.setVariable("status", event.getNewStatus());

            emailService.sendEmail(
                    studentEmail,
                    "Application Submitted — UniAdmit",
                    "application-submitted",
                    context);

        } catch (Exception e) {
            log.error("Failed to process APPLICATION_SUBMITTED event: {}", e.getMessage());
            // Don't rethrow — message will be acked and not requeued
            // In production, publish to DLQ for manual review
        }
    }

    // ── Listen: status changed ────────────────────────────────────────────────
    @RabbitListener(queues = "q.status.changed")
    public void onStatusChanged(ApplicationEvent event) {
        log.info("Received STATUS_CHANGED event: {} → {} for application: {}",
                event.getOldStatus(), event.getNewStatus(), event.getApplicationId());

        try {
            String studentEmail = studentLookupService.getStudentEmail(
                    event.getStudentId().toString());

            Context context = new Context();
            context.setVariable("applicationId", event.getApplicationId().toString());
            context.setVariable("courseName", event.getCourseName());
            context.setVariable("oldStatus", event.getOldStatus());
            context.setVariable("newStatus", event.getNewStatus());

            String subject = buildStatusSubject(event.getNewStatus());

            emailService.sendEmail(
                    studentEmail,
                    subject,
                    "status-changed",
                    context);

        } catch (Exception e) {
            log.error("Failed to process STATUS_CHANGED event: {}", e.getMessage());
        }
    }

    // ── Listen: user registered ───────────────────────────────────────────────
    @RabbitListener(queues = "q.user.registered")
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("Received USER_REGISTERED event for: {}", event.getEmail());

        try {
            Context context = new Context();
            context.setVariable("email", event.getEmail());

            emailService.sendEmail(
                    event.getEmail(),
                    "Welcome to UniAdmit!",
                    "welcome",
                    context);

        } catch (Exception e) {
            log.error("Failed to process USER_REGISTERED event: {}", e.getMessage());
        }
    }

    private String buildStatusSubject(String status) {
        return switch (status) {
            case "ACCEPTED"          -> "Congratulations! Your Application Has Been Accepted — UniAdmit";
            case "REJECTED"          -> "Application Status Update — UniAdmit";
            case "UNDER_REVIEW"      -> "Your Application Is Under Review — UniAdmit";
            case "DOCUMENTS_PENDING" -> "Action Required: Documents Needed — UniAdmit";
            default                  -> "Application Status Update — UniAdmit";
        };
    }
}


