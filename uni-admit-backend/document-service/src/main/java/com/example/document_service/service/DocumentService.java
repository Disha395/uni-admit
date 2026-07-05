package com.example.document_service.service;

import com.example.document_service.dto.DocumentResponse;
import com.example.document_service.entity.DocumentMetadata;
import com.example.document_service.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final MinioService minioService;

    @Value("${minio.default-bucket}")
    private String defaultBucket;

    // Allowed file types
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    // Max file size: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // ── Upload document ───────────────────────────────────────────────────────
    @Transactional
    public DocumentResponse uploadDocument(
            String studentId,
            String applicationId,
            String documentType,
            MultipartFile file) {

        // Validate file
        validateFile(file);

        UUID studentUUID = UUID.fromString(studentId);
        UUID applicationUUID = UUID.fromString(applicationId);
        DocumentMetadata.DocumentType docType = parseDocumentType(documentType);

        // Build unique object name: applicationId/docType/originalFilename
        String objectName = applicationId + "/" + documentType.toLowerCase()
                + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        // Upload to MinIO
        minioService.uploadFile(defaultBucket, objectName, file);

        // Save metadata to DB
        DocumentMetadata metadata = DocumentMetadata.builder()
                .applicationId(applicationUUID)
                .studentId(studentUUID)
                .fileName(objectName)
                .originalFileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .bucketName(defaultBucket)
                .objectName(objectName)
                .documentType(docType)
                .build();

        documentRepository.save(metadata);
        log.info("Document uploaded: {} for application: {}", metadata.getId(), applicationId);

        String downloadUrl = minioService.generatePresignedUrl(defaultBucket, objectName);
        return DocumentResponse.from(metadata, downloadUrl);
    }

    // ── Get document by ID with fresh pre-signed URL ──────────────────────────
    public DocumentResponse getDocument(String documentId) {
        DocumentMetadata metadata = findById(documentId);
        String downloadUrl = minioService.generatePresignedUrl(
                metadata.getBucketName(), metadata.getObjectName());
        return DocumentResponse.from(metadata, downloadUrl);
    }

    // ── Get all documents for an application ──────────────────────────────────
    public List<DocumentResponse> getDocumentsByApplication(String applicationId) {
        return documentRepository.findByApplicationId(UUID.fromString(applicationId))
                .stream()
                .map(doc -> {
                    String url = minioService.generatePresignedUrl(
                            doc.getBucketName(), doc.getObjectName());
                    return DocumentResponse.from(doc, url);
                })
                .collect(Collectors.toList());
    }

    // ── Get all documents for a student ───────────────────────────────────────
    public List<DocumentResponse> getDocumentsByStudent(String studentId) {
        return documentRepository.findByStudentId(UUID.fromString(studentId))
                .stream()
                .map(doc -> {
                    String url = minioService.generatePresignedUrl(
                            doc.getBucketName(), doc.getObjectName());
                    return DocumentResponse.from(doc, url);
                })
                .collect(Collectors.toList());
    }

    // ── Delete document ───────────────────────────────────────────────────────
    @Transactional
    public void deleteDocument(String documentId, String studentId) {
        DocumentMetadata metadata = findById(documentId);

        // Only the owner can delete their document
        if (!metadata.getStudentId().equals(UUID.fromString(studentId))) {
            throw new IllegalArgumentException("Not authorized to delete this document");
        }

        minioService.deleteFile(metadata.getBucketName(), metadata.getObjectName());
        documentRepository.delete(metadata);
        log.info("Document deleted: {}", documentId);
    }

    // ── Private helpers ───────────────────────────────────────────────────────
    private DocumentMetadata findById(String documentId) {
        return documentRepository.findById(UUID.fromString(documentId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Document not found: " + documentId));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 10MB");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException(
                    "File type not allowed. Allowed types: PDF, JPG, PNG");
        }
    }

    private DocumentMetadata.DocumentType parseDocumentType(String documentType) {
        try {
            return DocumentMetadata.DocumentType.valueOf(documentType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid document type: " + documentType
                            + ". Allowed: TRANSCRIPT, ID_PROOF, CERTIFICATE, PHOTO, OTHER");
        }
    }
}
