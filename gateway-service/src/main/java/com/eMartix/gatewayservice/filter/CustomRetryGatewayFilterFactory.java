package com.eMartix.gatewayservice.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.HasRouteId;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Component
@Slf4j
public class CustomRetryGatewayFilterFactory extends AbstractGatewayFilterFactory<CustomRetryGatewayFilterFactory.Config> {

    public CustomRetryGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        // Cho phép cấu hình ngắn gọn trong application.yml
        return Arrays.asList("retries", "firstBackoff", "maxBackoff", "retryOnStatuses");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Tạo cấu hình retry backoff
            Retry retrySpec = Retry.backoff(config.getRetries(), Duration.ofMillis(config.getFirstBackoff()))
                    .maxBackoff(Duration.ofMillis(config.getMaxBackoff()))
                    .jitter(config.isJitter() ? 0.5 : 0.0)
                    .filter(throwable -> shouldRetry(throwable, exchange, config))
                    .doBeforeRetry(signal -> logRetryAttempt(signal.totalRetries(), exchange));

            // Áp dụng retry vào filter chain
            return chain.filter(exchange)
                    .retryWhen(retrySpec)
                    .onErrorResume(throwable -> {
                        logMaxRetriesExceeded(config.getRetries(), exchange, throwable);
                        return Mono.error(throwable);
                    });
        };
    }

    public GatewayFilter apply(Consumer<Config> consumer) {
        Config config = newConfig();
        consumer.accept(config);
        return apply(config);
    }

    private void logRetryAttempt(long retryCount, ServerWebExchange exchange) {
        log.info("Retry attempt {} for {} {}", retryCount + 1, exchange.getRequest().getMethod(), exchange.getRequest().getURI());
    }

    private void logMaxRetriesExceeded(int maxRetries, ServerWebExchange exchange, Throwable throwable) {
        log.info("Failed after {} retry attempts for {} {}: {}", maxRetries, exchange.getRequest().getMethod(), exchange.getRequest().getURI(), throwable.getMessage());
    }

    private boolean shouldRetry(Throwable throwable, ServerWebExchange exchange, Config config) {
        // Không retry cho phương thức không idempotent (POST, PUT, DELETE) nếu cấu hình không cho phép
        HttpMethod method = exchange.getRequest().getMethod();
        if (!config.isRetryOnMutation() &&
                (HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method) || HttpMethod.DELETE.equals(method))) {
            log.error("Not retrying non-idempotent method: {}", method);
            return false;
        }

        // Retry nếu là lỗi IO
        if (throwable instanceof IOException) {
            log.error("Will retry because IOException: {}", throwable.getMessage());
            return true;
        }

        // Kiểm tra HTTP status
        HttpStatus status = (HttpStatus) exchange.getResponse().getStatusCode();
        if (status != null && config.getRetryableStatuses().contains(status)) {
            log.info("Will retry because of HTTP status: {}", status);
            return true;
        }

        log.error("Not retrying - throwable: {}{}", throwable.getClass().getSimpleName(), status != null ? ", status: " + status : "");
        return false;
    }

    public static class Config implements HasRouteId {
        private String routeId;
        private int retries = 3;
        private long firstBackoff = 100;
        private long maxBackoff = 2000;
        private double backoffFactor = 2.0;
        private boolean jitter = false;
        private Set<HttpStatus> retryableStatuses = new HashSet<>(Arrays.asList(
                HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.BAD_GATEWAY,
                HttpStatus.SERVICE_UNAVAILABLE,
                HttpStatus.GATEWAY_TIMEOUT
        ));
        private boolean retryOnMutation = false; // mặc định không retry các phương thức POST/PUT/DELETE

        @Override
        public String getRouteId() {
            return routeId;
        }

        @Override
        public void setRouteId(String routeId) {
            this.routeId = routeId;
        }

        public int getRetries() {
            return retries;
        }

        public void setRetries(int retries) {
            this.retries = retries;
        }

        public long getFirstBackoff() {
            return firstBackoff;
        }

        public void setFirstBackoff(long firstBackoff) {
            this.firstBackoff = firstBackoff;
        }

        public long getMaxBackoff() {
            return maxBackoff;
        }

        public void setMaxBackoff(long maxBackoff) {
            this.maxBackoff = maxBackoff;
        }

        public double getBackoffFactor() {
            return backoffFactor;
        }

        public void setBackoffFactor(double backoffFactor) {
            this.backoffFactor = backoffFactor;
        }

        public boolean isJitter() {
            return jitter;
        }

        public void setJitter(boolean jitter) {
            this.jitter = jitter;
        }

        public Set<HttpStatus> getRetryableStatuses() {
            return retryableStatuses;
        }

        public void setRetryableStatuses(Set<HttpStatus> retryableStatuses) {
            this.retryableStatuses = retryableStatuses;
        }

        public void setStatuses(HttpStatus... statuses) {
            this.retryableStatuses = new HashSet<>(Arrays.asList(statuses));
        }

        public boolean isRetryOnMutation() {
            return retryOnMutation;
        }

        public void setRetryOnMutation(boolean retryOnMutation) {
            this.retryOnMutation = retryOnMutation;
        }

        public void setBackoff(Duration firstBackoff, Duration maxBackoff, double factor, boolean jitter) {
            this.firstBackoff = firstBackoff.toMillis();
            this.maxBackoff = maxBackoff.toMillis();
            this.backoffFactor = factor;
            this.jitter = jitter;
        }
    }
}