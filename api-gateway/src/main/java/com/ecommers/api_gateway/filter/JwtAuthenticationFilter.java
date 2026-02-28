package com.ecommers.api_gateway.filter;


import com.ecommers.api_gateway.Config.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             WebFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        log.info("Incoming request path: {}", path);

        if (path.startsWith("/auth")) {
            log.info("Auth endpoint - skipping JWT validation");
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null) {
            log.warn("Authorization header missing");
            return chain.filter(exchange);
        }

        if (!authHeader.startsWith("Bearer ")) {
            log.warn("Authorization header does not start with Bearer");
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);
        log.info("Extracted token: {}", token);

        try {

            Claims claims = jwtUtil.validateToken(token);
            log.info("JWT validated successfully");

            String userEmail = claims.getSubject();
            log.info("User email from token: {}", userEmail);

            List<Map<String, String>> roles =
                    claims.get("roles", List.class);

            log.info("Roles from token: {}", roles);

            List<SimpleGrantedAuthority> authorities =
                    roles.stream()
                            .map(map -> new SimpleGrantedAuthority(
                                    map.get("authority")))
                            .collect(Collectors.toList());

            log.info("Authorities mapped: {}", authorities);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userEmail,
                            null,
                            authorities
                    );

            ServerHttpRequest mutatedRequest =
                    exchange.getRequest()
                            .mutate()
                            .header("X-User-Email", userEmail)
                            .build();

            log.info("Authentication set in SecurityContext");

            return chain.filter(
                    exchange.mutate()
                            .request(mutatedRequest)
                            .build()
            ).contextWrite(
                    ReactiveSecurityContextHolder
                            .withAuthentication(authentication)
            );

        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage(), e);
            return chain.filter(exchange);
        }
    }
}