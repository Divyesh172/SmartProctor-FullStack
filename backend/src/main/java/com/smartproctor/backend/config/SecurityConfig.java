package com.smartproctor.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // <--- CONNECTING CORS HERE
            .authorizeHttpRequests(auth -> auth
                // 1. PUBLIC ENDPOINTS
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/exam/active").permitAll()
                .requestMatchers("/api/exam/report-cheat").permitAll()
                .requestMatchers("/api/exam/status").permitAll() // <--- Allow Dashboard to check status

                // 2. ADMIN ENDPOINTS
                .requestMatchers("/api/exam/create").hasRole("ADMIN")

                // 3. ALL OTHERS LOCKED
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {});

        return http.build();
    }

    // --- THE NEW CORS CONFIGURATION ---
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // This explicitly allows ANY connection, but using the "Patterns" method
        // which fixes the "allowCredentials" error.
        configuration.setAllowedOriginPatterns(List.of("*"));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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