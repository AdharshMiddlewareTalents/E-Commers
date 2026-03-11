package com.ecommers.auth_service.service;

import com.ecommers.auth_service.exception.OtpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisOtpService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final long OTP_EXPIRY_MiNUTES = 5;
    private static final long RESEND_BLOCK_MINUTES = 1;

    public void saveOtp(String email,String otp){

        String key = "otp:"+email.trim().toLowerCase();

        log.info("saving Otp to Redis with key: {} ",key);

        redisTemplate.opsForValue()
                .set(key,otp, Duration.ofMinutes(OTP_EXPIRY_MiNUTES));
    }

    public String getOtp(String email){
        return redisTemplate.opsForValue()
                .get("otp:" + email.trim().toLowerCase());
    }


    public void deleteOtp(String email){
        redisTemplate.delete("otp:" + email.trim().toLowerCase());
    }


    public void checkResendThrottle(String email){

        String key = "otp:resend:" +email.trim().toLowerCase();

        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))){
            throw new OtpException("Please wait before requesting otp again");
        }

        redisTemplate.opsForValue()
                .set(key,"blocked",Duration.ofMinutes(RESEND_BLOCK_MINUTES));
    }

}
