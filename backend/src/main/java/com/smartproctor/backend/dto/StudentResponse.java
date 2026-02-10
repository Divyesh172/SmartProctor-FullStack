package com.smartproctor.backend.dto;

import com.smartproctor.backend.model.Student.ExamStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {

    // ==========================================
    // 1. Core Identity
    // ==========================================
    private Long id;
    private String fullName;
    private String email;
    private String profileImageUrl; // URL to the face reference image

    // ==========================================
    // 2. Exam Context (Flattened for UI convenience)
    // ==========================================
    private String examCode;
    private String examTitle;
    private Long examSessionId;

    // ==========================================
    // 3. Mobile Sentinel (The Unique Feature)
    // ==========================================
    // The string to be converted into a QR Code on the React Screen
    private String mobilePairingCode;

    // UI Trigger: If true, hide QR code and show "Device Connected"
    private boolean isMobileConnected;

    // ==========================================
    // 4. Proctoring Status (The "Health" Bar)
    // ==========================================
    private ExamStatus status; // REGISTERED, IN_PROGRESS, SUBMITTED, TERMINATED

    private int strikeCount;       // Raw number of warnings
    private double suspicionScore; // 0.0 to 100.0 (AI Risk Assessment)

    private boolean isBanned;
    private String banReason;

    // ==========================================
    // 5. Audit & Forensics
    // ==========================================
    private String ipAddress;
    private LocalDateTime registeredAt;
    private LocalDateTime lastActivityAt;
}