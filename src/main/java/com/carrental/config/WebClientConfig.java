package com.carrental.config;

import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebClientConfig {

    private final ApplicationProperties props;

    private HttpClient baseHttpClient() {
        ConnectionProvider provider = ConnectionProvider.builder("fixed")
                .maxConnections(50)
                .pendingAcquireTimeout(Duration.ofSeconds(5))
                .build();

        return HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000) // connection timeout
                .responseTimeout(Duration.ofSeconds(10));            // read timeout
    }

    private ExchangeFilterFunction loggingFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            System.out.println("WEBCLIENT REQUEST: " + clientRequest.method() + " " + clientRequest.url());
            return Mono.just(clientRequest);
        });
    }

    @Bean
    @Qualifier("carPricingWebClient")
    public WebClient carPricingWebClient(WebClient.Builder builder) {
        String url = props.getCarPricingApi().getBaseUrl();
        log.info("Creating carPricingWebClient with baseUrl={}", url);
        return builder
                .clientConnector(new ReactorClientHttpConnector(baseHttpClient()))
                .baseUrl(props.getCarPricingApi().getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(loggingFilter())
                .build();
    }

    @Bean
    @Qualifier("drivingLicenseWebClient")
    public WebClient drivingLicenseWebClient(WebClient.Builder builder) {
        String url = props.getDrivingLicenseApi().getBaseUrl();
        log.info("Creating drivingLicenseWebClient with baseUrl={}", url);
        return builder
                .clientConnector(new ReactorClientHttpConnector(baseHttpClient()))
                .baseUrl(props.getDrivingLicenseApi().getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(loggingFilter())
                .build();
    }
}
