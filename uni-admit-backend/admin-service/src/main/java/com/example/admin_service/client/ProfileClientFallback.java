package com.example.admin_service.client;

import com.example.admin_service.dto.ProfileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProfileClientFallback implements ProfileClient {

    @Override
    public ProfileResponse getProfile(String profileId) {
        log.warn("ProfileClient fallback: getProfile for {}", profileId);
        return null;
    }
}
