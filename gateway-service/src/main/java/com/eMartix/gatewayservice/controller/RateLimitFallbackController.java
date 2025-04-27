package com.eMartix.gatewayservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/rate-limit")
public class RateLimitFallbackController {

    @GetMapping("/exceeded")
    public Mono<ResponseEntity<Map<String, Object>>> rateLimitExceeded() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("code", HttpStatus.TOO_MANY_REQUESTS.value());
        response.put("message", "Rate limit exceeded. Please try again later.");
        response.put("timestamp", System.currentTimeMillis());

        return Mono.just(ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(response));
    }
}