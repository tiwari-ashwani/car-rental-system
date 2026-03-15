package com.carrental;

import com.carrental.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
public class CarRentalApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarRentalApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CarRentalApplication.class, args);
    }
}
