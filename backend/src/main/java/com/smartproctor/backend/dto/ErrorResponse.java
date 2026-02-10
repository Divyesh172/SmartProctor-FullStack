package com.smartproctor.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Don't send null fields (keeps JSON clean)
public class ErrorResponse {

    // ==========================================
    // 1. Standard HTTP Context
    // ==========================================
    private LocalDateTime timestamp;
    private int status;       // e.g., 400, 401, 500
    private String error;     // e.g., "Bad Request"

    // ==========================================
    // 2. Human-Readable Details
    // ==========================================
    private String message;   // e.g., "Exam Code not found"
    private String path;      // e.g., "/api/students/join"

    // ==========================================
    // 3. Validation Breakdown (The "Professional" Touch)
    // ==========================================
    // Used when @Valid fails on a DTO (e.g., RegisterStudentRequest)
    // Key = Field Name (e.g., "email"), Value = Error (e.g., "must be a valid email")
    private Map<String, String> validationErrors;

    // ==========================================
    // 4. Constructors for Convenience
    // ==========================================

    // Simple Error (for Runtime Exceptions)
    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    // Validation Error (for Form Submissions)
    public ErrorResponse(int status, String error, String message, String path, Map<String, String> validationErrors) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.validationErrors = validationErrors;
    }
}