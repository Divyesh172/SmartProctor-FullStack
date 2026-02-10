package com.smartproctor.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long professorId;
    private String email;
    private String role; // "PROFESSOR" or "ADMIN"

    public JwtAuthResponse(String accessToken, Long professorId, String email, String role) {
        this.accessToken = accessToken;
        this.professorId = professorId;
        this.email = email;
        this.role = role;
    }
}