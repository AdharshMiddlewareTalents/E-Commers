package com.ecommers.product_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtUtil {

    @Value("${jwt.access-secret}")
    private String accessSecret;

    @Value("${jwt.service-secret}")
    private String serviceSecret;

    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(
                        Keys.hmacShaKeyFor(
                                accessSecret.getBytes(StandardCharsets.UTF_8)
                        )
                )
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Claims validateServiceToken(String token) {
        Key key = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(serviceSecret)
        );

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .requireIssuer("order-service")
                .requireAudience("product-service")
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}