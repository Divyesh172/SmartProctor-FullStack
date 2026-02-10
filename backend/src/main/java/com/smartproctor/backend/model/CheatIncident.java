package com.smartproctor.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cheat_incidents")
public class CheatIncident {

    // ==========================================
    // 1. Identification
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // 2. The Nature of the Incident
    // ==========================================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheatType cheatType;

    @Column(columnDefinition = "TEXT")
    private String description; // e.g., "Mobile phone detected in top-right quadrant"

    // How sure is the AI? (0.0 to 1.0)
    // Use this to color-code the dashboard (Red = High, Yellow = Low)
    private double confidenceScore;

    // ==========================================
    // 3. Digital Evidence (The "Smoking Gun")
    // ==========================================
    // URL to the specific frame/image where the cheat happened.
    // The Python AI should upload the frame to Cloudinary/S3 and send this URL.
    @Column(columnDefinition = "TEXT")
    private String snapshotUrl;

    // ==========================================
    // 4. Professor's Verdict
    // ==========================================
    // Default is PENDING. Professor can click "Dismiss" (FALSE_POSITIVE) or "Confirm" (VERIFIED).
    @Enumerated(EnumType.STRING)
    private IncidentStatus status = IncidentStatus.PENDING_REVIEW;

    // ==========================================
    // 5. Context & Relationships
    // ==========================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_session_id", nullable = false)
    @JsonIgnore
    private ExamSession examSession;

    // ==========================================
    // 6. Audit
    // ==========================================
    @CreationTimestamp
    private LocalDateTime detectedAt;

    // ==========================================
    // Enums
    // ==========================================
    public enum CheatType {
        NO_FACE_DETECTED,       // Student left the room
        MULTIPLE_FACES_DETECTED,// Impersonation or help
        LOOKING_AWAY,           // Constant looking at notes
        MOBILE_PHONE_DETECTED,  // **The Unique Sentinel Feature**
        TAB_SWITCH,             // Browser focus lost
        COPY_PASTE_DETECTED,    // Inhuman typing speed
        SUSPICIOUS_AUDIO,       // Whispering detected
        UNAUTHORIZED_OBJECT     // Books, Calculator, etc.
    }

    public enum IncidentStatus {
        PENDING_REVIEW, // AI flagged it, waiting for human check
        VERIFIED,       // Professor confirmed it was cheating
        FALSE_POSITIVE  // Professor dismissed it (e.g., lighting issue)
    }
}