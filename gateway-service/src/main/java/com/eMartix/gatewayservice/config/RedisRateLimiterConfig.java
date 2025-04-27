package com.eMartix.gatewayservice.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class RedisRateLimiterConfig {

    /**
     * Cấu hình Redis Rate Limiter cho API sản phẩm
     * Cho phép 20 request/giây, với burst tối đa 40 request
     */
    @Bean(name = "productApiRateLimiter")
    @Primary
    public RedisRateLimiter productApiRateLimiter() {
        return new RedisRateLimiter(20, 40);
    }

    /**
     * Cấu hình Redis Rate Limiter cho API giỏ hàng
     * Cho phép 10 request/giây, với burst tối đa 20 request
     */
    @Bean(name = "cartApiRateLimiter")
    public RedisRateLimiter cartApiRateLimiter() {
        return new RedisRateLimiter(10, 20);
    }

    /**
     * Cấu hình Redis Rate Limiter cho API đơn hàng
     * Cho phép 5 request/giây, với burst tối đa 10 request
     */
    @Bean(name = "orderApiRateLimiter")
    public RedisRateLimiter orderApiRateLimiter() {
        return new RedisRateLimiter(5, 10);
    }

    /**
     * Cấu hình Redis Rate Limiter cho API xác thực
     * Cho phép 3 request/giây, với burst tối đa 5 request
     */
    @Bean(name = "authApiRateLimiter")
    public RedisRateLimiter authApiRateLimiter() {
        return new RedisRateLimiter(3, 5);
    }

    /**
     * Cấu hình Redis Rate Limiter mặc định
     * Cho phép 50 request/giây, với burst tối đa 100 request
     */
    @Bean(name = "defaultRateLimiter")
    public RedisRateLimiter defaultRateLimiter() {
        return new RedisRateLimiter(50, 100);
    }

    /**
     * Key resolver dựa trên địa chỉ IP của client
     */
    @Bean(name = "ipKeyResolver")
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            return Mono.just(ip);
        };
    }

    /**
     * Key resolver dựa trên user principal (cần xác thực)
     */
    @Bean(name = "userKeyResolver")
    public KeyResolver userKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .map(principal -> principal.getName())
                .defaultIfEmpty("anonymous");
    }

    /**
     * Key resolver dựa trên path của API
     */
    @Bean
    @Primary
    public KeyResolver apiPathKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest().getPath().value();
            return Mono.just(path);
        };
    }

}

