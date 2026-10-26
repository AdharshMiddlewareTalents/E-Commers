package com.ecommers.auth_service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.access-secret}")
    private String accessSecret;

    @Value("${jwt.refresh-secret}")
    private String refreshSecret;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    // ================= ACCESS TOKEN =================

    public String generateAccessToken(UserDetails userDetails) {

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(
                        Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    // ================= REFRESH TOKEN =================

    public String generateRefreshToken(UserDetails userDetails) {

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(
                        Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    // ================= EXTRACT USERNAME FROM REFRESH =================

    public String extractUsernameFromRefresh(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(
                        Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8))
                )
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractUsernameFromAccess(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(
                        Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8))
                )
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

}
