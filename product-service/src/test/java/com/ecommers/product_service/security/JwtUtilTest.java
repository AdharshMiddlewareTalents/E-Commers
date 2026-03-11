package com.ecommers.product_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private final String SECRET = "C7K4w+RBnJfIr1TZgb/JUWMiASrTq4cIWxkpExNtwxFYIfENDMUOY+RxRZKe4P8Yv7ky6+98bZYP1k6ot5lr5A==";

    @BeforeEach
    void setUp(){
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil,"accessSecret",SECRET);
    }

    @Test
    void validateToken_shouldReturnClaims_whenTokenIsValid() {

        String token = Jwts.builder()
                .setSubject("admin@test.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+60000))
                .signWith(
                        Keys.hmacShaKeyFor(
                                SECRET.getBytes(StandardCharsets.UTF_8)
                        )
                )
                .compact();

        Claims claims = jwtUtil.validateToken(token);

        assertEquals("admin@test.com",claims.getSubject());

    }

    @Test
    void validateToken_shouldThrowException_whenSignatureIsInvalid() {

        String wrongSecret = "anotherstrongsecretkeyanotherstrong";

        String token = Jwts.builder()
                .setSubject("user@test.com")
                .signWith(
                        Keys.hmacShaKeyFor(
                                wrongSecret.getBytes(StandardCharsets.UTF_8)
                        )
                )
                .compact();

        assertThrows(Exception.class, () ->
                jwtUtil.validateToken(token)
        );
    }

    @Test
    void validateToken_shouldThrowException_whenTokenIsExpired() {

        String token = Jwts.builder()
                .setSubject("admin@test.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 60000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(
                        Keys.hmacShaKeyFor(
                                SECRET.getBytes(StandardCharsets.UTF_8)
                        )
                )
                .compact();

        assertThrows(ExpiredJwtException.class, () ->
                jwtUtil.validateToken(token)
        );
    }
}