package com.example.admission_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Links to profile_db — no FK, different DB
    @Column(nullable = false)
    private UUID studentId;

    // Links to profile_db student_profiles — no FK
    @Column(nullable = false)
    private UUID profileId;

    @Column(nullable = false)
    private String courseName;

    private String university;

    private Integer intakeYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.DRAFT;

    private String rejectionReason;

    private String adminComments;

    // Tracks who last updated the status (admin userId)
    private UUID reviewedBy;

    private Instant submittedAt;
    private Instant reviewedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // ── State Machine ─────────────────────────────────────────────────────────
    public enum ApplicationStatus {
        DRAFT,              // created but not submitted
        SUBMITTED,          // submitted — async processing starts
        DOCUMENTS_PENDING,  // submitted but docs not uploaded yet
        UNDER_REVIEW,       // admin is reviewing
        ACCEPTED,           // final state — accepted
        REJECTED            // final state — rejected
    }

    // Valid transitions — enforced in service layer
    public boolean canTransitionTo(ApplicationStatus newStatus) {
        return switch (this.status) {
            case DRAFT             -> newStatus == ApplicationStatus.SUBMITTED;
            case SUBMITTED         -> newStatus == ApplicationStatus.DOCUMENTS_PENDING
                    || newStatus == ApplicationStatus.UNDER_REVIEW;
            case DOCUMENTS_PENDING -> newStatus == ApplicationStatus.UNDER_REVIEW;
            case UNDER_REVIEW      -> newStatus == ApplicationStatus.ACCEPTED
                    || newStatus == ApplicationStatus.REJECTED;
            case ACCEPTED, REJECTED -> false; // terminal states
        };
    }
}