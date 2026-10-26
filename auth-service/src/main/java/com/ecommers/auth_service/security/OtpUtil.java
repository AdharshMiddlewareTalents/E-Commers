package com.ecommers.auth_service.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;

@Slf4j
@Component
public class OtpUtil {

    private static final SecureRandom random = new SecureRandom();

    public String generateOtp(){


        log.info("otp generation begins");

        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);

    }
}
