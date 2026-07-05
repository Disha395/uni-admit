package com.example.document_service.dto;

import com.example.document_service.entity.DocumentMetadata;
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
public class DocumentResponse {

    private UUID id;
    private UUID applicationId;
    private UUID studentId;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private String documentType;
    private String status;
    private String downloadUrl;   // pre-signed MinIO URL
    private Instant uploadedAt;

    public static DocumentResponse from(DocumentMetadata doc, String downloadUrl) {
        return DocumentResponse.builder()
                .id(doc.getId())
                .applicationId(doc.getApplicationId())
                .studentId(doc.getStudentId())
                .originalFileName(doc.getOriginalFileName())
                .contentType(doc.getContentType())
                .fileSize(doc.getFileSize())
                .documentType(doc.getDocumentType().name())
                .status(doc.getStatus().name())
                .downloadUrl(downloadUrl)
                .uploadedAt(doc.getUploadedAt())
                .build();
    }
}
