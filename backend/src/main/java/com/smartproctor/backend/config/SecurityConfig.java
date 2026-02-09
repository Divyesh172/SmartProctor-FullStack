package com.smartproctor.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF so Go can POST data
            .authorizeHttpRequests(auth -> auth
                // 1. PUBLIC ENDPOINTS (Login, Active Exams, & GO Reporting)
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/exam/active").permitAll()
                .requestMatchers("/api/exam/report-cheat").permitAll() // <--- CRITICAL for Go Engine
                
                // 2. ADMIN ENDPOINTS
                .requestMatchers("/api/exam/create").hasRole("ADMIN")
                
                // 3. ALL OTHERS LOCKED
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {}); 

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.withDefaultPasswordEncoder()
            .username("admin")
            .password("password")
            .roles("ADMIN")
            .build();
            
        return new InMemoryUserDetailsManager(admin);
    }
}