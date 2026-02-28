//package com.ecommers.product_service.security;
//
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.List;
//
//@Component
//public class HeaderAuthenticationFilter extends OncePerRequestFilter {
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain)
//            throws ServletException, IOException {
//
//        System.out.println(SecurityContextHolder.getContext().getAuthentication());
//
//        String userId = request.getHeader("X-User-Id");
//        String role = request.getHeader("X-User-Role");
//
//        if (userId != null && role != null) {
//
//            UsernamePasswordAuthenticationToken authentication =
//                    new UsernamePasswordAuthenticationToken(
//                            userId,
//                            null,
//                            List.of(new SimpleGrantedAuthority(role))
//                    );
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//            System.out.println("security:"+SecurityContextHolder.getContext().getAuthentication());
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}
