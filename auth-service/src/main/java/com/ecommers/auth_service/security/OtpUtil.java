package com.ecommers.auth_service.security;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;

@Component
public class OtpUtil {

    private static final SecureRandom random = new SecureRandom();

    public String generateOtp(){

        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);

    }
}
