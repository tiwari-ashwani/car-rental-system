package com.carrental.dto;

import java.util.UUID;

public record VehicleResponse(
        UUID id,
        String type,
        VehicleSegment segment,
        String vin,
        Integer modelYear,
        VehicleStatus status
) {}
