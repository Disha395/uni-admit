package com.example.admission_service.repository;

import com.example.admission_service.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    List<Application> findByStudentId(UUID studentId);

    boolean existsByStudentIdAndStatusNotIn(UUID studentId, List<Application.ApplicationStatus> statuses);
}
