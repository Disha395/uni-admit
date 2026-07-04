package com.example.profile_service.dto;

import com.example.profile_service.entity.StudentProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private UUID id;
    private UUID userId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String country;
    private Double tenthPercentage;
    private Double twelfthPercentage;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public static ProfileResponse from(StudentProfile profile) {
        return ProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .dateOfBirth(profile.getDateOfBirth())
                .phone(profile.getPhone())
                .address(profile.getAddress())
                .city(profile.getCity())
                .state(profile.getState())
                .country(profile.getCountry())
                .tenthPercentage(profile.getTenthPercentage())
                .twelfthPercentage(profile.getTwelfthPercentage())
                .status(profile.getStatus().name())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
