package com.eMartix.gatewayservice.helper;

import org.springframework.stereotype.Component;

@Component
public class GenerateRequestId {
    public String generateRequestId() {
        return String.format("request-id: %s", System.currentTimeMillis());
    }
}
