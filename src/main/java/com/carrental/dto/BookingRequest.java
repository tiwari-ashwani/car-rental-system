package com.carrental.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * Request payload for booking a rental car.
 */
public record BookingRequest(
        @NotBlank(message = "licenseNumber is required")
        @Pattern(regexp = "^[A-Z]{2}\\d{9}$",
                message = "Invalid license format. Expected e.g. DL123456789")
        String licenseNumber,

        @NotBlank(message = "customerName is required as per license")
        String customerName,

        @NotNull(message = "age is required")
        @Min(value = 0, message = "age must be non-negative")
        Integer age,

        @NotNull(message = "reservationStartDate is required")
        LocalDate reservationStartDate,

        @NotNull(message = "reservationEndDate is required")
        LocalDate reservationEndDate,

        @NotNull(message = "segment is required")
        VehicleSegment segment,

        @NotNull(message = "vehicle identification number is required")
        String vin

) {}
