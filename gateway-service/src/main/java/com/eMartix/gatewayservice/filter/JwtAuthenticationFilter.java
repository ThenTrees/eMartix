package com.eMartix.gatewayservice.filter;

import com.eMartix.commons.utils.AppContants;
import com.eMartix.commons.utils.CustomHeaders;
import com.eMartix.gatewayservice.helper.GenerateRequestId;
import com.eMartix.gatewayservice.redis.RedisService;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final RedisService redisService;
    private final GenerateRequestId generateRequestId;

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Lấy đường dẫn của request
        String path = exchange.getRequest().getPath().toString();
        if (isAuthRequest(exchange)) {
            // Bypass nhưng vẫn set một security context rỗng để Spring Security không chặn
            return setSecurityContextAndContinueWithoutToken(exchange, chain);
        }
        String token = extractJwtFromRequest(exchange);

        if (token == null || !isValidToken(token)) {
            return Mono.error(new JwtException("Invalid or missing JWT token"));
        }

        // check token is exist in redis
        Claims claims = extractClaims(token);
        if (!redisService.checkExistToken(claims.get("username").toString(), token)){
            return Mono.error(new JwtException("Token not found in redis"));
        }
        List<SimpleGrantedAuthority> authorities = extractAuthoritiesFromClaims(claims);
        Authentication authentication = createAuthentication(claims, authorities);
        return setSecurityContextAndContinue(exchange, chain, authentication, token, claims.getSubject());
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
            Jws<Claims> claimsJws =  Jwts.parser().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
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
            return Jwts.parser().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody();
        } catch (Exception e) {
            log.error("Error parsing token: {}", e.getMessage());
            throw new JwtException("Invalid token", e);
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

    private Mono<Void> setSecurityContextAndContinue(ServerWebExchange exchange, WebFilterChain chain, Authentication authentication, String token, String userId) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Tạo request mới với các header bổ sung
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(CustomHeaders.AUTHENTICATION, "Bearer " + token)
                .header(CustomHeaders.X_API_KEY, AppContants.X_API_KEY)
                .header(CustomHeaders.X_REQUEST_ID, generateRequestId.generateRequestId())
                .header(CustomHeaders.X_AUTH_USER_AUTHORITIES, String.valueOf(authentication.getAuthorities()))
                .header(CustomHeaders.X_AUTH_USER_ID, userId)
                .build();
        // Tạo exchange mới với request đã mutate
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();
        return chain.filter(mutatedExchange)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
    }

    private Mono<Void> setSecurityContextAndContinueWithoutToken(ServerWebExchange exchange, WebFilterChain chain) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("anonymous", null, Collections.emptyList()));

        // Tạo request mới với các header bổ sung
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(CustomHeaders.X_API_KEY, AppContants.X_API_KEY)
                .header(CustomHeaders.X_REQUEST_ID, generateRequestId.generateRequestId())
                .header(CustomHeaders.X_AUTH_USER_AUTHORITIES, String.valueOf(context.getAuthentication().getAuthorities()))
                .header(CustomHeaders.X_AUTH_USER_ID, "")
                .build();
        // Tạo exchange mới với request đã mutate
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();
        return chain.filter(mutatedExchange)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
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
            "/api/v1/categories/search",
            // actuator
            "/actuator/**"
    );
}
