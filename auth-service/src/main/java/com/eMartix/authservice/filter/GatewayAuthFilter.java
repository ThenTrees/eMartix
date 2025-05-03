package com.eMartix.authservice.filter;

import com.eMartix.commons.utils.AppContants;
import com.eMartix.commons.utils.CustomHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class GatewayAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri.contains("/actuator/health")){
            filterChain.doFilter(request, response);
            return;
        }

        // Kiểm tra API key trong header
        String apiKey = request.getHeader(CustomHeaders.X_API_KEY);
        // Kiểm tra xem API key có hợp lệ không

        if (!isValidApiKey(apiKey)) {
            log.error("[GatewayAuthFilter] Invalid API Key: {}", apiKey);
            throw new ServletException("Invalid API Key");
        }
        filterChain.doFilter(request, response);
    }

    private boolean isValidApiKey(String apiKey) {
        return AppContants.X_API_KEY.equals(apiKey);
    }
}
