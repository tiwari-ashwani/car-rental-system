package com.carrental.dto;

import jakarta.validation.constraints.*;

public record CustomerRequest(
        @NotBlank String firstName,

        @NotBlank String lastName,

        @Min(0) @Max(100) Integer age,

        @Email @NotBlank String email,

        @NotBlank String drivingLicenseNumber,

        @NotBlank @Pattern(regexp = "^(\\+?[0-9\\- ]{7,20})?$", message = "Invalid phone number format")
        String phoneNumber
) {}
