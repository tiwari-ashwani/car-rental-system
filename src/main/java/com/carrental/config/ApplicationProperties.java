package com.carrental.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

    private CarPricingApi carPricingApi = new CarPricingApi();
    private DrivingLicenseApi drivingLicenseApi = new DrivingLicenseApi();

    @Data
    public static class CarPricingApi {
        private String baseUrl;
    }

    @Data
    public static class DrivingLicenseApi {
        private String baseUrl;
    }
}

