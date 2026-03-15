package com.carrental.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BookingResponse(
        UUID bookingId,
        String licenseNumber,
        String customerName,
        Integer age,
        LocalDate reservationStartDate,
        LocalDate reservationEndDate,
        VehicleSegment segment,
        BigDecimal rentalPrice // total price for the reservation
) {}

