package com.example.admin_service.dto;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

// Mirrors Profile Service ProfileResponse — used for Feign deserialization
@Data
public class ProfileResponse {

    private UUID id;
    private UUID userId;
    private String firstName;
    private String lastName;
    private String phone;
    private String city;
    private String state;
    private String country;
    private Double tenthPercentage;
    private Double twelfthPercentage;
    private String status;
    private Instant createdAt;
}
