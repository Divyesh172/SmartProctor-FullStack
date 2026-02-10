package com.smartproctor.backend.security;

import com.smartproctor.backend.model.Professor;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    // Helper to generate the signing key
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ==========================================
    // 1. Generate Token
    // ==========================================
    public String generateToken(Professor professor) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(professor.getEmail())
                .claim("role", professor.getRole().name())
                .claim("id", professor.getId())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256) // Updated Syntax
                .compact();
    }

    // ==========================================
    // 2. Validate Token
    // ==========================================
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey()) // Updated Syntax
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    // ==========================================
    // 3. Extract Email (Username)
    // ==========================================
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // Updated Syntax
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}