package com.company.erp.config;

import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

@Configuration
public class AvailabilityConfiguration {

    @Bean
    public HealthIndicator readinessStateHealthIndicator() {
        return () -> Health.up()
                .withDetail("readinessState", ReadinessState.ACCEPTING_TRAFFIC)
                .build();
    }

    @Bean
    public HealthIndicator livenessStateHealthIndicator() {
        return () -> Health.up()
                .withDetail("livenessState", LivenessState.CORRECT)
                .build();
    }
}