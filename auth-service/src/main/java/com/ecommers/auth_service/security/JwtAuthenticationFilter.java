package com.ecommers.auth_service.security;

import com.ecommers.auth_service.service.CustomerUserDetailService;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomerUserDetailService userDetailService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();

        String token = null;

        if(cookies!=null){
            for (Cookie cookie : cookies){
                if(cookie.getName().equals("accessToken")){
                    token = cookie.getValue();
                }
            }
        }

        if(token != null){
            String username =
                    jwtUtil.extractUsernameFromAccess(token);

            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null){

                UserDetails userDetails =
                        userDetailService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authToken);

            }
        }

        filterChain.doFilter(request,response);
    }
}
