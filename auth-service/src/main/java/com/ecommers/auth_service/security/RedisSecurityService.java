package com.ecommers.auth_service.security;

import com.ecommers.auth_service.exception.AccountNotActiveException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisSecurityService {
    private final RedisTemplate<String,String> redisTemplate;

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_WINDOW_MINUTES = 10;

    public void recordFailedLogin(String email){
        String key = "login:attemts:"+ email;

        Long attempts =
                redisTemplate.opsForValue().increment(key);

        if (attempts != null && attempts ==1){
            redisTemplate.expire(key,
                    Duration.ofMinutes(BLOCK_WINDOW_MINUTES));
        }

        if (attempts!=null && attempts>MAX_ATTEMPTS){
            throw new AccountNotActiveException(
                    "Too many login attempts. Try again later"
            );

        }
    }

    public void clearFailedLogin(String email){
        redisTemplate.delete("login:attemts:"+email);
    }
}
