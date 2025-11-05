package com.carrental.dto;

import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String firstName,
        String lastName,
        Integer age,
        String email,
        String drivingLicenseNumber,
        String phoneNumber
) {}
