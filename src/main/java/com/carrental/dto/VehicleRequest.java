package com.carrental.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VehicleRequest(
        @NotBlank String type,
        @NotNull VehicleSegment segment,
        @NotBlank String vin,
        @Min(1886) @Max(3000) Integer modelYear,
        @NotNull VehicleStatus status
) {}
