package com.example.profile_service.controller;

import com.example.profile_service.dto.ProfileRequest;
import com.example.profile_service.dto.ProfileResponse;
import com.example.profile_service.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // Create profile — userId comes from X-User-Id header injected by Gateway
    @PostMapping
    public ResponseEntity<ProfileResponse> createProfile(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.createProfile(userId, request));
    }

    // Get own profile
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }

    // Get profile by profileId — used by Admin Service and Admission Service
    @GetMapping("/{profileId}")
    public ResponseEntity<ProfileResponse> getProfileById(@PathVariable String profileId) {
        return ResponseEntity.ok(profileService.getProfileById(profileId));
    }

    // Update own profile
    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateProfile(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(userId, request));
    }
}
