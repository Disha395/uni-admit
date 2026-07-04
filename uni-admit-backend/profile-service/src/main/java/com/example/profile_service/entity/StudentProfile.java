package com.example.profile_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "student_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Links to auth_db users table — no FK since different DB
    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private LocalDate dateOfBirth;

    @Column(unique = true)
    private String phone;

    private String address;
    private String city;
    private String state;
    private String country;

    // Academic history
    private Double tenthPercentage;
    private Double twelfthPercentage;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProfileStatus status = ProfileStatus.INCOMPLETE;

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

    public enum ProfileStatus {
        INCOMPLETE,   // created but missing fields
        COMPLETE      // all required fields filled — can apply
    }
}