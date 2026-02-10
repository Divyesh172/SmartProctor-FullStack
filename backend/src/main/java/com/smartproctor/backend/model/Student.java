package com.smartproctor.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "students")
public class Student {

    // ==========================================
    // 1. Identity & Auth
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    // Use this if you implement individual logins later.
    // If not, it can remain null or be removed.
    @JsonIgnore
    private String password;

    // ==========================================
    // 2. Biometric & Security Data
    // ==========================================
    // The Reference Image URL (uploaded during registration)
    @Column(columnDefinition = "TEXT")
    private String profileImageUrl;

    // Optional: Store raw face embeddings for server-side verification (Advanced)
    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    private String faceEmbeddings;

    // To prevent device swapping during exam
    private String browserFingerprint;

    private String ipAddress;

    // ==========================================
    // 3. The "Mobile Sentinel" (Unique Feature)
    // ==========================================
    // The code displayed as a QR on the screen.
    // Phone scans this -> sends WS message to Go Engine -> Updates this field.
    @Column(unique = true)
    private String mobilePairingCode;

    // Is the phone currently sending a heartbeat?
    private boolean isMobileConnected = false;

    // ==========================================
    // 4. Cheat Metrics (The "Report Card")
    // ==========================================
    private int strikeCount = 0;

    // A continuous score (0.0 to 100.0) based on AI confidence.
    // E.g., Looking away = +5, Phone detected = +20
    private double suspicionScore = 0.0;

    // Tracks how many times they verified "alt-tab"
    private int tabSwitchCount = 0;

    // ==========================================
    // 5. Exam State & Lifecycle
    // ==========================================
    @Enumerated(EnumType.STRING)
    private ExamStatus status = ExamStatus.REGISTERED;

    private boolean isBanned = false;

    private String banReason; // e.g., "Multiple faces detected"

    // The Exam they are taking
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_session_id", nullable = false)
    @JsonIgnore
    private ExamSession examSession;

    // ==========================================
    // 6. Timestamps (Audit)
    // ==========================================
    @CreationTimestamp
    private LocalDateTime registeredAt;

    @UpdateTimestamp
    private LocalDateTime lastActivityAt; // Helps detect if they pulled the plug

    // ==========================================
    // Inner Enum for Status
    // ==========================================
    public enum ExamStatus {
        REGISTERED,  // Just joined the lobby
        IN_PROGRESS, // Currently writing
        SUBMITTED,   // Finished legitimately
        TERMINATED   // Kicked out by proctor/AI
    }
}