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
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "exam_sessions", indexes = {
    @Index(name = "idx_exam_code", columnList = "examCode")
})
public class ExamSession {

    // ==========================================
    // 1. Identification
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // e.g., "Finals: Data Structures"

    @Column(nullable = false)
    private String subjectCode; // e.g., "CS-302"

    @Column(unique = true, nullable = false)
    private String examCode; // The unique 6-char join code (e.g., "XY7-99A")

    // Optional: A password students must type AFTER joining the room
    private String examSecretKey;

    @Column(columnDefinition = "TEXT")
    private String instructions; // "No calculators, keep phone visible..."

    // ==========================================
    // 2. Scheduling
    // ==========================================
    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    private int durationMinutes; // e.g., 90 minutes

    // ==========================================
    // 3. Security Configurations (The "Smart" Part)
    // ==========================================

    // UNIQUE FEATURE TOGGLE: Does this exam require the "Mobile Sentinel"?
    private boolean isMobileSentinelActive = true;

    // How strict is the AI? (Low = Forgiving, High = Flag everything)
    @Enumerated(EnumType.STRING)
    private SensitivityLevel sensitivity = SensitivityLevel.MEDIUM;

    // How many strikes before auto-termination? (0 = Infinite)
    private int maxWarnings = 3;

    // ==========================================
    // 4. State Management
    // ==========================================
    private boolean isPublished = false; // Students can't see it until published

    private boolean isActive = true; // "Emergency Stop" button for Professor

    // ==========================================
    // 5. Relationships
    // ==========================================

    // The Creator
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    @JsonIgnore // Security: Don't leak professor details to every student
    private Professor professor;

    // The Participants
    @OneToMany(mappedBy = "examSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Student> students = new ArrayList<>();

    // The Cheat Logs (Aggregate view for the Professor Dashboard)
    @OneToMany(mappedBy = "examSession", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<CheatIncident> incidents = new ArrayList<>();

    // ==========================================
    // 6. Audit
    // ==========================================
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ==========================================
    // Inner Enum for AI Strictness
    // ==========================================
    public enum SensitivityLevel {
        LOW,    // Only flag obvious absence
        MEDIUM, // Flag looking away
        HIGH    // Flag eye movement + background noise
    }
}