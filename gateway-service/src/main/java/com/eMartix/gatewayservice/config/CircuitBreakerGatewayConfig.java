package com.eMartix.gatewayservice.config;

import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import java.time.Duration;

@Configuration
public class CircuitBreakerGatewayConfig {
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .slidingWindowSize(10)
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofSeconds(5))
                        .permittedNumberOfCallsInHalfOpenState(3)
                        .minimumNumberOfCalls(5)
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(2))
                        .build())
                .build());
    }

//    @Bean
//    public Customizer<ReactiveResilience4JCircuitBreakerFactory> specificCustomizer() {
//        return factory -> factory.configure(builder -> builder
//                .circuitBreakerConfig(CircuitBreakerConfig.custom()
//                        .slidingWindowSize(20)
//                        .failureRateThreshold(40)
//                        .waitDurationInOpenState(Duration.ofSeconds(10))
//                        .build())
//                .timeLimiterConfig(TimeLimiterConfig.custom()
//                        .timeoutDuration(Duration.ofSeconds(3))
//                        .build()), "serviceB");
//    }
}
