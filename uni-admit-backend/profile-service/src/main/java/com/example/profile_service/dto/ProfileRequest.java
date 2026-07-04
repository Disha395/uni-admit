package com.example.profileservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private LocalDate dateOfBirth;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String country;
    private Double tenthPercentage;
    private Double twelfthPercentage;
}
