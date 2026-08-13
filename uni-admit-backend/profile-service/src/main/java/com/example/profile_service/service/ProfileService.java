package com.example.profile_service.service;

import com.example.profile_service.client.AuthClient;
import com.example.profile_service.dto.ProfileRequest;
import com.example.profile_service.dto.ProfileResponse;
import com.example.profile_service.entity.StudentProfile;
import com.example.profile_service.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final StudentProfileRepository profileRepository;
    private final AuthClient authClient;

    // ── Create profile ────────────────────────────────────────────────────────
    @Transactional
    public ProfileResponse createProfile(String userId, ProfileRequest request) {
        UUID userUUID = UUID.fromString(userId);

        // Check profile doesn't already exist
        if (profileRepository.existsByUserId(userUUID)) {
            throw new IllegalArgumentException("Profile already exists for user: " + userId);
        }

        // Validate user exists in Auth Service (with circuit breaker fallback)
        boolean exists = authClient.userExists(userId);
        if (!exists) {
            throw new IllegalArgumentException("User not found in Auth Service: " + userId);
        }

        StudentProfile profile = StudentProfile.builder()
                .userId(userUUID)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .tenthPercentage(request.getTenthPercentage())
                .twelfthPercentage(request.getTwelfthPercentage())
                .build();

        // Auto-compute status
        profile.setStatus(computeStatus(profile));
        profileRepository.save(profile);

        return ProfileResponse.from(profile);
    }

    // ── Get profile by userId ─────────────────────────────────────────────────
    public ProfileResponse getProfileByUserId(String userId) {
        StudentProfile profile = profileRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for user: " + userId));
        return ProfileResponse.from(profile);
    }

    // ── Get profile by profileId ──────────────────────────────────────────────
    public ProfileResponse getProfileById(String profileId) {
        StudentProfile profile = profileRepository.findById(UUID.fromString(profileId))
                .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + profileId));
        return ProfileResponse.from(profile);
    }

    // ── Update profile ────────────────────────────────────────────────────────
    @Transactional
    public ProfileResponse updateProfile(String userId, ProfileRequest request) {
        StudentProfile profile = profileRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for user: " + userId));

        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setPhone(request.getPhone());
        profile.setAddress(request.getAddress());
        profile.setCity(request.getCity());
        profile.setState(request.getState());
        profile.setCountry(request.getCountry());
        profile.setTenthPercentage(request.getTenthPercentage());
        profile.setTwelfthPercentage(request.getTwelfthPercentage());
        profile.setStatus(computeStatus(profile));

        profileRepository.save(profile);
        return ProfileResponse.from(profile);
    }

    // ── Compute profile completion status ─────────────────────────────────────
    private StudentProfile.ProfileStatus computeStatus(StudentProfile profile) {
        boolean complete = profile.getFirstName() != null
                && profile.getLastName() != null
                && profile.getDateOfBirth() != null
                && profile.getPhone() != null
                && profile.getAddress() != null
                && profile.getCity() != null
                && profile.getState() != null
                && profile.getCountry() != null
                && profile.getTenthPercentage() != null
                && profile.getTwelfthPercentage() != null;

        return complete
                ? StudentProfile.ProfileStatus.COMPLETE
                : StudentProfile.ProfileStatus.INCOMPLETE;
    }
}
