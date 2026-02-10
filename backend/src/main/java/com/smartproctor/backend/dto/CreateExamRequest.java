package com.smartproctor.backend.dto;

import com.smartproctor.backend.model.ExamSession.SensitivityLevel;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExamRequest {

    // ==========================================
    // 1. Basic Exam Info
    // ==========================================
    @NotBlank(message = "Exam title is required")
    private String title;       // e.g., "Finals: Data Structures"

    @NotBlank(message = "Subject code is required")
    private String subjectCode; // e.g., "CS-302"

    private String instructions; // e.g., "No calculators allowed"

    // Optional: A secondary password for the exam room (extra security)
    private String examSecretKey;

    // ==========================================
    // 2. Scheduling
    // ==========================================
    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;

    @Min(value = 10, message = "Exam duration must be at least 10 minutes")
    private int durationMinutes;

    // ==========================================
    // 3. AI & Security Configuration (The "Smart" Control)
    // ==========================================

    // Feature Toggle: Should students be forced to scan QR with phone?
    // Default to true, but Prof can disable for "Open Book" exams.
    private boolean isMobileSentinelActive = true;

    // AI Strictness: LOW, MEDIUM, HIGH
    // If null, Service layer defaults to MEDIUM.
    private SensitivityLevel sensitivity;

    // Auto-Termination limit.
    // 0 = Infinite warnings (never kick). 3 = Standard.
    @Min(value = 0, message = "Max warnings cannot be negative")
    private int maxWarnings = 3;
}