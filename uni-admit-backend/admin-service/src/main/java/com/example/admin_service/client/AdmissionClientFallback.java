package com.example.admin_service.client;

import com.example.admin_service.dto.ApplicationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AdmissionClientFallback implements AdmissionClient {

    @Override
    public List<ApplicationResponse> getAllApplications() {
        log.warn("AdmissionClient fallback: getAllApplications");
        return Collections.emptyList();
    }


    @Override
    public ApplicationResponse getApplication(String applicationId) {
        log.warn("AdmissionClient fallback: getApplication for {}", applicationId);
        return null;
    }

    @Override
    public List<ApplicationResponse> getStudentApplications(String studentId) {
        log.warn("AdmissionClient fallback: getStudentApplications for {}", studentId);
        return Collections.emptyList();
    }

    @Override
    public ApplicationResponse updateStatus(String applicationId, String adminId,
                                            String roles, Map<String, String> request) {
        log.warn("AdmissionClient fallback: updateStatus for {}", applicationId);
        return null;
    }
}
