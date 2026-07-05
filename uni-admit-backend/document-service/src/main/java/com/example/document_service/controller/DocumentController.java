package com.example.document_service.controller;

import com.example.document_service.dto.DocumentResponse;
import com.example.document_service.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/docs")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    // Upload document — multipart/form-data
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(
            @RequestHeader("X-User-Id") String studentId,
            @RequestParam("applicationId") String applicationId,
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.uploadDocument(studentId, applicationId, documentType, file));
    }

    // Get document by ID — returns metadata + fresh pre-signed download URL
    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable String documentId) {
        return ResponseEntity.ok(documentService.getDocument(documentId));
    }

    // Get all documents for an application
    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<DocumentResponse>> getByApplication(
            @PathVariable String applicationId) {
        return ResponseEntity.ok(documentService.getDocumentsByApplication(applicationId));
    }

    // Get all documents for logged-in student
    @GetMapping("/my")
    public ResponseEntity<List<DocumentResponse>> getMyDocuments(
            @RequestHeader("X-User-Id") String studentId) {
        return ResponseEntity.ok(documentService.getDocumentsByStudent(studentId));
    }

    // Delete document
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable String documentId,
            @RequestHeader("X-User-Id") String studentId) {
        documentService.deleteDocument(documentId, studentId);
        return ResponseEntity.noContent().build();
    }
}

