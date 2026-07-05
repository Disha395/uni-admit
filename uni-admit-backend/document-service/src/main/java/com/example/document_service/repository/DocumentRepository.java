package com.example.document_service.repository;

import com.example.document_service.entity.DocumentMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentMetadata, UUID> {

    List<DocumentMetadata> findByApplicationId(UUID applicationId);

    List<DocumentMetadata> findByStudentId(UUID studentId);

    boolean existsByApplicationIdAndDocumentType(UUID applicationId,
                                                 DocumentMetadata.DocumentType documentType);
}
