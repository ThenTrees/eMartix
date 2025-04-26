package com.eMartix.gatewayservice.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsGlobalConfiguration {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        corsConfig.addAllowedOrigin("http://localhost:3000"); // Cho phép frontend của bạn
        corsConfig.addAllowedMethod("*"); // Cho phép tất cả method: GET, POST, PUT, DELETE...
        corsConfig.addAllowedHeader("*"); // Cho phép tất cả header
        corsConfig.setAllowCredentials(true); // Nếu cần gửi cookie/token

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig); // Áp dụng cho tất cả các route

        return new CorsWebFilter(source);
    }
}