package com.carrental.client.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@Builder
@Jacksonized
public record RateResponse(
        @JsonProperty("category")
        String category,

        @JsonProperty("ratePerDay")
        BigDecimal ratePerDay
) {}
