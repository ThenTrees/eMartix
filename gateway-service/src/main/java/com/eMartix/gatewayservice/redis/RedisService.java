package com.eMartix.gatewayservice.redis;

public interface RedisService {
    boolean checkExistToken(String username,String key);
}
