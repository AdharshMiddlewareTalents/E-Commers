package com.ecommers.product_service;


import com.ecommers.product_service.security.JwtAuthenticationFilter;
import com.ecommers.product_service.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

	private JwtAuthenticationFilter filter;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private FilterChain filterChain;

	@Mock
	private Claims claims;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		filter = new JwtAuthenticationFilter(jwtUtil);
		SecurityContextHolder.clearContext();
	}

	@Test
	void shouldContinueFilterWhenNoCookiePresent() throws Exception {

		when(request.getCookies()).thenReturn(null);

		filter.doFilter(request, response, filterChain);

		verify(filterChain).doFilter(request, response);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void shouldAuthenticateWhenTokenValid() throws Exception {

		String token = "validToken";

		Cookie cookie = new Cookie("accessToken", token);

		when(request.getCookies()).thenReturn(new Cookie[]{cookie});

		when(jwtUtil.validateToken(token)).thenReturn(claims);

		when(claims.getSubject()).thenReturn("test@email.com");

		List<Map<String, String>> roles =
				List.of(Map.of("authority", "ROLE_ADMIN"));

		when(claims.get("roles")).thenReturn(roles);

		filter.doFilter(request, response, filterChain);

		assertNotNull(SecurityContextHolder.getContext().getAuthentication());

		assertEquals(
				"test@email.com",
				SecurityContextHolder.getContext()
						.getAuthentication()
						.getPrincipal()
		);

		verify(filterChain).doFilter(request, response);
	}

	@Test
	void shouldContinueFilterWhenJwtValidationFails() throws Exception {

		String token = "invalidToken";

		Cookie cookie = new Cookie("accessToken", token);

		when(request.getCookies()).thenReturn(new Cookie[]{cookie});

		when(jwtUtil.validateToken(token))
				.thenThrow(new RuntimeException("Invalid token"));

		filter.doFilter(request, response, filterChain);

		verify(filterChain).doFilter(request, response);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}
}
