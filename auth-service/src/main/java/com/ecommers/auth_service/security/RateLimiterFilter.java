package com.ecommers.auth_service.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiterFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    private Bucket createBucket(int limit, Duration duration){
        Bandwidth bandwidth = Bandwidth.classic(
                limit,
                Refill.greedy(limit, duration)
        );

        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }

    private Bucket resolveBucket(String key, int limit, Duration duration){
        return bucketCache.computeIfAbsent(
                key,
                k -> createBucket(limit, duration)
        );
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String ip = request.getRemoteAddr();
        String path = request.getRequestURI();

        int limit;
        Duration duration = Duration.ofMinutes(1);

        if(path.equals("/auth/login")){
            limit = 5;
        }else if(path.equals("/auth/register")) {
            limit = 3;
        } else if (path.equals("/auth/verify-otp")){
            limit = 5;
        } else if (path.equals("/auth/resend-otp")) {
            limit = 3;
        } else if (path.equals("/auth/refresh")) {
            limit= 10;
            
        }else {
            limit = 60;
        }

        String key = ip + ":" + path;

        Bucket bucket = resolveBucket(key,limit,duration);

        if(bucket.tryConsume(1)){
            filterChain.doFilter(request,response);
        }
        else {
            response.setStatus(429);
            response.getWriter().write("Too many requests");
        }
    }
}
