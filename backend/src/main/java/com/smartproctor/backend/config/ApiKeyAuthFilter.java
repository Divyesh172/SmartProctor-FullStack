package com.smartproctor.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Value("${app.proctor.api-key}")
    private String principalRequestHeader; // The Header Name, e.g. "X-API-KEY"

    @Value("${app.proctor.api-secret}")
    private String principalRequestValue;  // The Secret Key

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Only apply this filter to Proctor endpoints
        if (path.startsWith("/api/proctor")) {
            String requestApiKey = request.getHeader(principalRequestHeader);

            if (principalRequestValue.equals(requestApiKey)) {
                // Key is valid, grant system access
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        "SystemAgent", null, AuthorityUtils.createAuthorityList("ROLE_PROCTOR"));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // Invalid Key
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid API Key");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}