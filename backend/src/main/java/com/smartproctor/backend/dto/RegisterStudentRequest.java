package com.smartproctor.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterStudentRequest {

    // ==========================================
    // 1. User Inputs (Form Data)
    // ==========================================
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Exam code is required")
    private String examCode; // The 6-char code provided by the Professor

    // Optional: If you implement a "Lobby Password" later
    private String examPassword;

    // ==========================================
    // 2. Device Forensics (Hidden Fields)
    // ==========================================
    // These are automatically populated by the Frontend (React)
    // using libraries like 'clientjs' or 'fingerprintjs'.

    @NotBlank(message = "Browser fingerprint is missing")
    private String browserFingerprint;

    // This is usually extracted from the HTTP Request Header in the Controller,
    // but can be explicitly passed if you are using a proxy/load balancer.
    private String ipAddress;
}