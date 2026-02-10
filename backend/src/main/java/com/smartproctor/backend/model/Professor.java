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
@Table(name = "professors")
public class Professor {

    // ==========================================
    // 1. Identity & Auth
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    // Unique Staff ID (e.g., "PROF-CS-2024-001")
    // Use this to look professional during the demo.
    @Column(unique = true)
    private String employeeId;

    // ==========================================
    // 2. Professional Context
    // ==========================================
    private String department; // e.g., "Computer Science", "Data Science"

    private String universityName;

    // ==========================================
    // 3. Security & Access Control
    // ==========================================
    // If false, they can login but CANNOT create exams.
    // Great feature: "Admin must verify professors before they can proctor."
    private boolean isVerified = false;

    // Role-based access (PROFESSOR, HEAD_OF_DEPT, ADMIN)
    // Useful if you want to add a "Super Admin" dashboard later.
    @Enumerated(EnumType.STRING)
    private Role role = Role.PROFESSOR;

    // ==========================================
    // 4. Relationships
    // ==========================================
    // One Professor manages MANY Exams
    @OneToMany(mappedBy = "professor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamSession> createdExams = new ArrayList<>();

    // ==========================================
    // 5. Audit Logs
    // ==========================================
    @CreationTimestamp
    private LocalDateTime joinedAt;

    @UpdateTimestamp
    private LocalDateTime lastLoginAt;

    // ==========================================
    // Inner Enum for Roles
    // ==========================================
    public enum Role {
        PROFESSOR,
        HEAD_OF_DEPT,
        ADMIN
    }
}