package com.example.admin_service.client;

import com.example.admin_service.dto.ProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "profile-service", fallback = ProfileClientFallback.class)
public interface ProfileClient {

    @GetMapping("/profile/{profileId}")
    ProfileResponse getProfile(@PathVariable("profileId") String profileId);
}
