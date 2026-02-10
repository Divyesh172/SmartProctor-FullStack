package com.smartproctor.backend.service;

import com.smartproctor.backend.dto.LoginRequest;
import com.smartproctor.backend.dto.RegisterProfessorRequest;
import com.smartproctor.backend.exception.ResourceNotFoundException;
import com.smartproctor.backend.exception.UnauthorizedException;
import com.smartproctor.backend.model.ExamSession;
import com.smartproctor.backend.model.Professor;
import com.smartproctor.backend.repository.CheatIncidentRepository;
import com.smartproctor.backend.repository.ExamSessionRepository;
import com.smartproctor.backend.repository.ProfessorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfessorService {

    private final ProfessorRepository professorRepository;
    private final ExamSessionRepository examSessionRepository;
    private final CheatIncidentRepository cheatIncidentRepository;
    private final PasswordEncoder passwordEncoder;

    // ==========================================
    // 1. Onboarding & Registration
    // ==========================================
    @Transactional
    public Professor registerProfessor(RegisterProfessorRequest request) {
        log.info("New Professor registration attempt: {}", request.getEmail());

        // Step 1: Duplicate Checks
        if (professorRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use.");
        }
        if (professorRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new IllegalArgumentException("Employee ID is already registered.");
        }

        // Step 2: Create Profile (Securely)
        Professor professor = Professor.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // CRITICAL: Never store plain text
                .employeeId(request.getEmployeeId())
                .department(request.getDepartment())
                .universityName(request.getUniversityName())
                // Default Security Settings
                .role(Professor.Role.PROFESSOR)
                .isVerified(false) // MUST be verified by Admin before creating exams
                .build();

        Professor savedProfessor = professorRepository.save(professor);
        log.info("Professor registered (Pending Verification): ID {}", savedProfessor.getId());

        return savedProfessor;
    }

    // ==========================================
    // 2. Authentication (The Gatekeeper)
    // ==========================================
    public Professor login(LoginRequest request) {
        Professor professor = professorRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid credentials."));

        // Step 1: Verify Password
        if (!passwordEncoder.matches(request.getPassword(), professor.getPassword())) {
            throw new UnauthorizedException("Invalid credentials.");
        }

        // Step 2: Verify Status (The Unique Feature)
        // If they are not verified, they can't access the dashboard.
        if (!professor.isVerified()) {
            log.warn("Unverified professor attempted login: {}", request.getEmail());
            throw new UnauthorizedException("Your account is pending administrative approval. Please contact IT.");
        }

        log.info("Professor logged in: {}", professor.getEmail());
        return professor;
    }

    // ==========================================
    // 3. Dashboard Analytics (The "Command Center")
    // ==========================================
    /**
     * Aggregates high-level stats for the dashboard homepage.
     * Prevents the frontend from making 5 different API calls.
     */
    public Map<String, Object> getProfessorDashboardStats(String email) {
        Professor professor = professorRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Professor not found"));

        List<ExamSession> exams = examSessionRepository.findByProfessorIdOrderByCreatedAtDesc(professor.getId());

        // Calculate Stats
        long totalExams = exams.size();
        long activeExams = exams.stream().filter(ExamSession::isActive).count();

        // Advanced: Count total students processed across all exams
        long totalStudentsProcessed = exams.stream()
                .mapToLong(e -> e.getStudents().size())
                .sum();

        // Advanced: Count total cheat incidents flagged
        long totalIncidents = exams.stream()
                .mapToLong(e -> cheatIncidentRepository.findByExamSessionIdOrderByDetectedAtDesc(e.getId()).size())
                .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("professorName", professor.getFullName());
        stats.put("department", professor.getDepartment());
        stats.put("totalExams", totalExams);
        stats.put("activeExams", activeExams);
        stats.put("totalStudentsEvaluated", totalStudentsProcessed);
        stats.put("totalCheatIncidents", totalIncidents);

        return stats;
    }

    // ==========================================
    // 4. Admin Features (For the "Super User")
    // ==========================================
    /**
     * Used by System Admins to verify new professors.
     */
    @Transactional
    public void approveProfessor(Long professorId) {
        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new ResourceNotFoundException("Professor not found"));

        professor.setVerified(true);
        professorRepository.save(professor);
        log.info("Professor verified by Admin: {}", professor.getEmail());
    }

    public List<Professor> getUnverifiedProfessors() {
        return professorRepository.findUnverifiedProfessors();
    }

    public Professor getProfessorByEmail(String email) {
        return professorRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Professor not found"));
    }
}