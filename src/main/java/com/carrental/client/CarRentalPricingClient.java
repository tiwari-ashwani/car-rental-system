package com.carrental.client;

import com.carrental.client.dto.RateRequest;
import com.carrental.client.dto.RateResponse;
import com.carrental.exception.InvalidCategoryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@Slf4j
public class CarRentalPricingClient {

    private final WebClient webClient;

    public CarRentalPricingClient(@Qualifier("carPricingWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Optional<RateResponse> getRateForCategory(String category) {
        try {
            RateRequest req = new RateRequest(category);
            var node = webClient.post()
                    .uri("/rental/rate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(req)
                    .retrieve()
                    .onStatus(status -> status.value() == 400 || status.value() == 404,
                            cr -> Mono.error(new InvalidCategoryException("Category not found: " + category)))
                    .bodyToMono(com.fasterxml.jackson.databind.JsonNode.class)
                    .block();

            if (node == null) return Optional.empty();

            String cat = node.path("category").asText(null);
            BigDecimal rate = node.path("ratePerDay").isNumber() ? node.path("ratePerDay").decimalValue() : null;
            if (rate == null) return Optional.empty();
            return Optional.of(new RateResponse(cat, rate));
        } catch (InvalidCategoryException e) {
            return Optional.empty();
        }  catch (WebClientResponseException e) {
            throw new RuntimeException("Pricing service error " , e);
        } catch (Exception e) {
            throw new ExternalServiceException("Pricing service failure", e);
        }
    }
    public static class InvalidCategoryException extends RuntimeException {
        public InvalidCategoryException(String msg) { super(msg); }
    }

    public static class ExternalServiceException extends RuntimeException {
        public ExternalServiceException(String msg, Throwable cause) { super(msg, cause); }
    }
}
