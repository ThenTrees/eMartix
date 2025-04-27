package com.eMartix.gatewayservice.config;

import com.eMartix.gatewayservice.filter.CustomRetryGatewayFilterFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.time.Duration;

@Configuration
//@RequiredArgsConstructor
public class EnhancedGatewayConfig {

    @Value("${gateway.x-api-key}")
    private String apiKey;

    private final CustomRetryGatewayFilterFactory retryGatewayFilterFactory;

//    @Qualifier("ipKeyResolver")

    private final KeyResolver ipKeyResolver;
//    @Qualifier("productApiRateLimiter")
    private final RedisRateLimiter productRateLimiter;
//    @Qualifier("cartApiRateLimiter")
    private final RedisRateLimiter cartRateLimiter;
//    @Qualifier("orderApiRateLimiter")
    private final RedisRateLimiter orderRateLimiter;
//    @Qualifier("authApiRateLimiter")
    private final RedisRateLimiter authRateLimiter;
//    @Qualifier("defaultRateLimiter")
    private final RedisRateLimiter defaultRateLimiter;

    @Autowired
    public EnhancedGatewayConfig(
            CustomRetryGatewayFilterFactory retryGatewayFilterFactory,
            @Qualifier("ipKeyResolver") KeyResolver ipKeyResolver,
            @Qualifier("productApiRateLimiter") RedisRateLimiter productRateLimiter,
            @Qualifier("cartApiRateLimiter") RedisRateLimiter cartRateLimiter,
            @Qualifier("orderApiRateLimiter") RedisRateLimiter orderRateLimiter,
            @Qualifier("authApiRateLimiter")RedisRateLimiter authRateLimiter,
            @Qualifier("defaultRateLimiter") RedisRateLimiter defaultRateLimiter
    ) {
        this.retryGatewayFilterFactory = retryGatewayFilterFactory;
        this.ipKeyResolver = ipKeyResolver;
        this.productRateLimiter = productRateLimiter;
        this.cartRateLimiter = cartRateLimiter;
        this.authRateLimiter = authRateLimiter;
        this.orderRateLimiter = orderRateLimiter;
        this.defaultRateLimiter = defaultRateLimiter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // PRODUCT-SERVICE với Rate Limiting
                .route("PRODUCT-SERVICE", r -> r
                        .path("/api/v1/products/**", "/api/v1/categories/**")
                        .filters(f -> f
                                // Áp dụng Rate Limiting
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(productRateLimiter)
                                        .setKeyResolver(ipKeyResolver)
                                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS)
                                        .setDenyEmptyKey(false))
                                // Áp dụng Retry
                                .filter(retryGatewayFilterFactory.apply(c -> {
                                    c.setRetries(3);
                                    c.setBackoff(Duration.ofMillis(200), Duration.ofSeconds(2), 2.0, true);
                                    c.setStatuses(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.BAD_GATEWAY,
                                            HttpStatus.SERVICE_UNAVAILABLE, HttpStatus.GATEWAY_TIMEOUT);
                                }))
                                // Áp dụng Circuit Breaker
                                .circuitBreaker(config -> config
                                        .setName("productServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/product"))
                        )
                        .uri("lb://PRODUCT-SERVICE")
                )

                // CART-SERVICE với Rate Limiting
                .route("CART-SERVICE", r -> r
                        .path("/api/v1/carts/**")
                        .filters(f -> f
                                // Áp dụng Rate Limiting
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(cartRateLimiter)
                                        .setKeyResolver(ipKeyResolver)
                                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS)
                                        .setDenyEmptyKey(false))
                                // Áp dụng Retry
                                .filter(retryGatewayFilterFactory.apply(c -> {
                                    c.setRetries(3);
                                    c.setBackoff(Duration.ofMillis(200), Duration.ofSeconds(2), 2.0, true);
                                    c.setStatuses(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.BAD_GATEWAY,
                                            HttpStatus.SERVICE_UNAVAILABLE, HttpStatus.GATEWAY_TIMEOUT);
                                    c.setRetryOnMutation(true);
                                }))
                                .circuitBreaker(config -> config
                                        .setName("cartServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/cart"))
                        )
                        .uri("lb://CART-SERVICE")
                )

                // ORDER-SERVICE với Rate Limiting
                .route("ORDER-SERVICE", r -> r
                        .path("/api/v1/order/**")
                        .filters(f -> f
                                // Áp dụng Rate Limiting
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(orderRateLimiter)
                                        .setKeyResolver(ipKeyResolver)
                                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS)
                                        .setDenyEmptyKey(false))
                                // Áp dụng Retry
                                .filter(retryGatewayFilterFactory.apply(c -> {
                                    c.setRetries(3);
                                    c.setBackoff(Duration.ofMillis(200), Duration.ofSeconds(2), 2.0, true);
                                    c.setStatuses(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.BAD_GATEWAY,
                                            HttpStatus.SERVICE_UNAVAILABLE, HttpStatus.GATEWAY_TIMEOUT);
                                }))
                                .circuitBreaker(config -> config
                                        .setName("orderServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/order"))
                        )
                        .uri("lb://ORDER-SERVICE")
                )

                // AUTH-SERVICE với Rate Limiting chặt chẽ hơn
                .route("AUTH-SERVICE", r -> r
                        .path("/api/v1/auth/**")
                        .filters(f -> f
                                .addRequestHeader("X-API-KEY", apiKey)
                                .rewritePath("/api/v1/auth/(?<segment>.*)", "/${segment}")
                                // Áp dụng Rate Limiting
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(authRateLimiter)
                                        .setKeyResolver(ipKeyResolver)
                                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS)
                                        .setDenyEmptyKey(false))
                                // Áp dụng Retry
                                .filter(retryGatewayFilterFactory.apply(c -> {
                                    c.setRetries(3);
                                    c.setBackoff(Duration.ofMillis(200), Duration.ofSeconds(2), 2.0, true);
                                    c.setStatuses(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.BAD_GATEWAY,
                                            HttpStatus.SERVICE_UNAVAILABLE, HttpStatus.GATEWAY_TIMEOUT);
                                }))
                                .circuitBreaker(config -> config
                                        .setName("authServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/auth"))
                        )
                        .uri("lb://AUTH-SERVICE")
                )

                // NOTI-SERVICE với Rate Limiting mặc định
                .route("NOTI-SERVICE", r -> r
                        .path("/api/v1/notifications/**")
                        .filters(f -> f
                                // Áp dụng Rate Limiting
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(defaultRateLimiter)
                                        .setKeyResolver(ipKeyResolver)
                                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS)
                                        .setDenyEmptyKey(false))
                                // Áp dụng Retry
                                .filter(retryGatewayFilterFactory.apply(c -> {
                                    c.setRetries(3);
                                    c.setBackoff(Duration.ofMillis(200), Duration.ofSeconds(2), 2.0, true);
                                    c.setStatuses(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.BAD_GATEWAY,
                                            HttpStatus.SERVICE_UNAVAILABLE, HttpStatus.GATEWAY_TIMEOUT);
                                }))
                                .circuitBreaker(config -> config
                                        .setName("notificationServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/notification"))
                        )
                        .uri("lb://NOTI-SERVICE")
                )

                .build();
    }
}