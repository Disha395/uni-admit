package com.example.document_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Links to admission_db applications — no FK, different DB
    @Column(nullable = false)
    private UUID applicationId;

    // Links to auth_db users — no FK, different DB
    @Column(nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    // MinIO bucket and object path
    @Column(nullable = false)
    private String bucketName;

    @Column(nullable = false)
    private String objectName;

    // SHA-256 checksum for integrity verification
    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DocumentType documentType = DocumentType.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.UPLOADED;

    @Column(nullable = false, updatable = false)
    private Instant uploadedAt;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = Instant.now();
    }

    public enum DocumentType {
        TRANSCRIPT,
        ID_PROOF,
        CERTIFICATE,
        PHOTO,
        OTHER
    }

    public enum DocumentStatus {
        UPLOADED,    // file is in MinIO
        VERIFIED,    // admin has verified the document
        REJECTED     // admin rejected the document
    }
}

