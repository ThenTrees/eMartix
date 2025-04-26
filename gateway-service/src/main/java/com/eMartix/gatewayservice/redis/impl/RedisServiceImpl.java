package com.eMartix.gatewayservice.redis.impl;

import com.eMartix.gatewayservice.redis.RedisService;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {
    private final StatefulRedisConnection<String, String> redisConnection;

    @Override
    public boolean checkExistToken(String username,String token) {
            String existToken = redisConnection.sync().get("accessToken:" + username);
        return token.equals(existToken);
    }

}
