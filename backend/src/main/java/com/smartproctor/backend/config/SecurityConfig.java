package com.smartproctor.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
		http
			//Disable CSRF (Cross-Site Request Forgery)
			.csrf(AbstractHttpConfigurer::disable)
			.cors(org.springframework.security.config.Customizer.withDefaults())
			.addFilterBefore(new ApiKeyAuthFilter(), UsernamePasswordAuthenticationFilter.class)
			
			.authorizeHttpRequests(auth -> auth
					// 1. PUBLIC ENDPOINTS (Everyone allowed)
					.requestMatchers(
							"/api/exam/register",
							"/api/exam/status",
							"/api/exam/active",
							"/v3/api-docs/**",
							"/api-docs/**",
							"/swagger-ui/**",
							"/swagger-ui.html"
							).permitAll()
					
					// 2. PROTECTED ENDPOINTS (NEED API KEY)
					.requestMatchers("/api/exam/report-cheat").authenticated()
					.requestMatchers("/api/exam/list-students").authenticated()
					.requestMatchers("/api/exam/create").authenticated()
					
					// 3. CATCH-ALL (Everything else is locked)
					.anyRequest().authenticated()
					);
		return http.build();
	}
	
	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails user = User.withDefaultPasswordEncoder()
				.username("admin")
				.password("password")
				.roles("USER")
				.build();
		return new InMemoryUserDetailsManager(user);
	}
}
