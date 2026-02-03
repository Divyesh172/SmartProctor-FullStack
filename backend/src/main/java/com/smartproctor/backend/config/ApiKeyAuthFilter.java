package com.smartproctor.backend.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApiKeyAuthFilter extends OncePerRequestFilter {
	private static final String HEADER_NAME = "X-API-KEY";
	private static final String VALID_API_KEY = "PROCTOR_SECURE_123";
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
		throws ServletException, IOException {
			// 1. Get the API Key from the header
			String apiKey = request.getHeader(HEADER_NAME);
			
			// 2. Validate it
			if (VALID_API_KEY.equals(apiKey)) {
				// If valid, tll Spring Security: "This guy is okay, he is a SYSTEM ADMIN"
				Authentication auth = new UsernamePasswordAuthenticationToken(
						"System", apiKey, AuthorityUtils.createAuthorityList("ROLE_SYSTEM"));
				SecurityContextHolder.getContext().setAuthentication(auth);
		}
			
			// 3. Continue (If invalid, the SecurityConfig will catch him later)
			filterChain.doFilter(request, response);
	}
}

