package com.example.admission_service.repository;

import com.example.admission_service.entity.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, UUID> {

    List<StatusHistory> findByApplicationIdOrderByChangedAtAsc(UUID applicationId);
}
