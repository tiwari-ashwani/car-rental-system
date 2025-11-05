package com.carrental.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.Optional;

@Component
public class DrivingLicenseClient {

    private final WebClient webClient;

    public DrivingLicenseClient(@Qualifier("drivingLicenseWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public record LicenseRequest(String licenseNumber) {}
    public record LicenseResponse(String ownerName, LocalDate expiryDate) {}

    public Optional<LicenseResponse> getLicenseDetails(String licenseNumber) {
        try {
            var node = webClient.post()
                    .uri("/license/details")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new LicenseRequest(licenseNumber))
                    .retrieve()
                    .onStatus(status -> status.value() == 404,
                            cr -> Mono.error(new LicenseNotFoundException("License not found: " + licenseNumber)))
                    .bodyToMono(com.fasterxml.jackson.databind.JsonNode.class)
                    .block(Duration.ofSeconds(5)); // avoid indefinite block

            if (node == null) return Optional.empty();

            String owner = node.path("ownerName").asText(null);
            String expiry = node.path("expiryDate").asText(null);
            LocalDate expiryDate = expiry != null ? LocalDate.parse(expiry, DateTimeFormatter.ISO_DATE) : null;
            return Optional.of(new LicenseResponse(owner, expiryDate));
        } catch (LicenseNotFoundException e) {
            return Optional.empty();
        } catch (WebClientResponseException e) {
            throw new ExternalServiceException("Driving license service error", e);
        } catch (Exception e) {
            throw new ExternalServiceException("Driving license service failure", e);
        }
    }

    public static class LicenseNotFoundException extends RuntimeException {
        public LicenseNotFoundException(String msg) { super(msg); }
    }

    public static class ExternalServiceException extends RuntimeException {
        public ExternalServiceException(String msg, Throwable cause) { super(msg, cause); }
    }
}
