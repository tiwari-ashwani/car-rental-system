package com.carrental.client.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;


public record LicenseResponse(
        @JsonProperty("ownerName")
        String ownerName,

        @JsonProperty("expiryDate")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate expiryDate
) {}
