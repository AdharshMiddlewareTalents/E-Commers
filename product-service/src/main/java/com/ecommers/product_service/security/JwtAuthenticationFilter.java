package com.ecommers.product_service.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();
        String method = request.getMethod();

        boolean internalRequest =
                path.matches("/internal/products/\\d+")
                        && "GET".equalsIgnoreCase(method)
                        || path.matches(
                        "/internal/products/\\d+/(reserve-stock|release-stock)"
                ) && "PATCH".equalsIgnoreCase(method);

        log.debug(
                "Incoming request: method={}, path={}, internalRequest={}",
                method, path, internalRequest
        );

        String token = internalRequest
                ? getBearerToken(request)
                : getTokenFromCookies(request);

        if (internalRequest && token == null) {
            log.warn("Missing service Bearer token for {}", path);
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Service authentication token is required"
            );
            return;
        }

        if (token != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = internalRequest
                        ? jwtUtil.validateServiceToken(token)
                        : jwtUtil.validateToken(token);

                String subject = claims.getSubject();

                if (subject == null || subject.isBlank()) {
                    response.sendError(
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "Invalid authentication token"
                    );
                    return;
                }

                List<SimpleGrantedAuthority> authorities;

                if (internalRequest) {
                    if (!"order-service".equals(subject)) {
                        response.sendError(
                                HttpServletResponse.SC_UNAUTHORIZED,
                                "Invalid service identity"
                        );
                        return;
                    }

                    authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_SERVICE")
                    );
                } else {
                    authorities = extractAuthorities(claims.get("roles"));
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                subject, null, authorities
                        );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);

                log.debug(
                        "JWT authenticated: subject={}, authorities={}, path={}",
                        subject, authorities, path
                );

            } catch (Exception exception) {
                SecurityContextHolder.clearContext();

                log.warn(
                        "JWT validation failed for {}: {}",
                        path, exception.getMessage()
                );

                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Invalid or expired authentication token"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> extractAuthorities(
            Object rolesClaim
    ) {
        if (!(rolesClaim instanceof List<?> roles)) {
            return Collections.emptyList();
        }

        return roles.stream()
                .map(role -> {
                    String authority;

                    if (role instanceof Map<?, ?> roleMap) {
                        Object value = roleMap.get("authority");
                        authority = value == null ? "" : value.toString();
                    } else {
                        authority = role == null ? "" : role.toString();
                    }

                    authority = authority.trim();

                    if (authority.isBlank()) {
                        return null;
                    }

                    if (!authority.startsWith("ROLE_")) {
                        authority = "ROLE_" + authority;
                    }

                    return new SimpleGrantedAuthority(authority);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String getBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header != null
                && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String token = header.substring(7).trim();
            return token.isEmpty() ? null : token;
        }

        return null;
    }

    private String getTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if ("accessToken".equals(cookie.getName())) {
                String token = cookie.getValue();
                return token == null || token.isBlank() ? null : token;
            }
        }

        return null;
    }
}