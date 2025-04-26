package com.eMartix.gatewayservice.filter;

import com.eMartix.gatewayservice.redis.RedisService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final RedisService redisService;

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${gateway.x-api-key}")
    private String X_API_KEY;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Lấy đường dẫn của request
        String path = exchange.getRequest().getPath().toString();

        if (isAuthRequest(exchange)) {
            // ✅ Bypass nhưng vẫn set một security context rỗng để Spring Security không chặn
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(new UsernamePasswordAuthenticationToken("anonymous", null, Collections.emptyList()));
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
        }
        String token = extractJwtFromRequest(exchange);


        if (token == null || !isValidToken(token)) {
            return Mono.error(new JwtException("Invalid or missing JWT token"));
        }

        // check token is exist in redis
        Claims claims = extractClaims(token);
        if (!redisService.checkExistToken(claims.getSubject(), token)){
            return Mono.error(new JwtException("Token not found in redis"));
        }
        List<SimpleGrantedAuthority> authorities = extractAuthoritiesFromClaims(claims);
        Authentication authentication = createAuthentication(claims, authorities);
        return setSecurityContextAndContinue(exchange, chain, authentication, token);
    }

    private boolean isAuthRequest(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().toString();
        return WHITE_LIST.stream().anyMatch(path::contains);
    }

    private String extractJwtFromRequest(ServerWebExchange exchange) {
        String bearerToken = exchange.getRequest().getHeaders().getFirst("Authorization");
        return bearerToken != null && bearerToken.startsWith("Bearer ") ? bearerToken.substring(7) : null;
    }

    private boolean isValidToken(String token) {
        try {
            Jws<Claims> claimsJws =  Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            Claims claims = claimsJws.getPayload();
            // Kiểm tra thời gian hết hạn
            Date expiration = claims.getExpiration();
            if (expiration != null && expiration.before(new Date())) {
                log.error("JWT token has expired");
                return false;
            }
            return true;
        } catch (JwtException e) {
            log.error("Invalid token: {}", e.getMessage());
            return false;
        }
    }

    private Claims extractClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getPayload();
        } catch (JwtException e) {
            throw new JwtException("Invalid JWT token", e);
        }
    }

    private List<SimpleGrantedAuthority> extractAuthoritiesFromClaims(Claims claims) {
        return Arrays.stream(claims.get("authorities").toString().split(","))
                        .filter(auth -> !auth.trim().isEmpty())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
    }

    private Authentication createAuthentication(Claims claims, List<SimpleGrantedAuthority> authorities) {
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
    }

    private Mono<Void> setSecurityContextAndContinue(ServerWebExchange exchange, WebFilterChain chain, Authentication authentication, String token) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Tạo request mới với các header bổ sung
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("Authorization", "Bearer " + token)
                .header("X-API-KEY", X_API_KEY)
                .build();

        // Tạo exchange mới với request đã mutate
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        // Tiếp tục xử lý request với mutatedExchange (không dùng session nữa)
        return chain.filter(mutatedExchange);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/auth/send-verification-otp",
            "/api/v1/auth/resent-otp",
            "/api/v1/auth/reset-password-request",
            "/api/v1/auth/verify-link",
            // product
            "/api/v1/products",
            "/api/v1/products/{productId}",
            "/api/v1/products/search",
            // categories
            "/api/v1/categories",
            "/api/v1/categories/{categoryId}",
            "/api/v1/categories/search"

    );
}
