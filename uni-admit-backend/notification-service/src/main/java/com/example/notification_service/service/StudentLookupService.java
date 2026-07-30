package com.example.notification_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// Looks up student email from Auth Service
// Simple RestTemplate call — no Feign needed since Notification has no web context
@Service
@Slf4j
public class StudentLookupService {

    // In a real setup this would call Auth Service via Feign or RestTemplate
    // For demo: return a placeholder email so the listener works without Auth Service
    // Replace with actual Feign call once Gateway is in place
    public String getStudentEmail(String studentId) {
        // TODO: replace with actual Feign call to auth-service
        // return authClient.getUserEmail(studentId);
        log.warn("StudentLookupService: returning placeholder email for studentId: {}", studentId);
        return "sample.demo395@gmail.com";   // placeholder for demo
    }
}

























