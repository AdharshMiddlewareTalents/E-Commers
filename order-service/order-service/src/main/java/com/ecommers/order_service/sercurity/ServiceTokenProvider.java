package com.ecommers.order_service.sercurity;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Component
public class ServiceTokenProvider {

    private final Key signingKey;

    public ServiceTokenProvider(
            @Value("${jwt.service-secret}") String serviceSecret) {

        byte[] keyBytes = Decoders.BASE64.decode(serviceSecret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken() {
        Instant now = Instant.now();

        return Jwts.builder()
                .setIssuer("order-service")
                .setAudience("product-service")
                .setSubject("order-service")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(300)))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }
}

