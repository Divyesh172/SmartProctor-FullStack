package com.smartproctor.backend.repository;

import com.smartproctor.backend.model.ExamSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {

    // ==========================================
    // 1. Core Access (Student Entry)
    // ==========================================
    // The main gate: Students enter via the unique Exam Code
    Optional<ExamSession> findByExamCode(String examCode);

    // Essential for your Code Generator logic to prevent duplicates
    boolean existsByExamCode(String examCode);

    // ==========================================
    // 2. Professor Dashboard
    // ==========================================
    // Show a Professor's exams, newest first (Good UX)
    List<ExamSession> findByProfessorIdOrderByCreatedAtDesc(Long professorId);

    // Separate "Live" exams from "Archived" history
    List<ExamSession> findByProfessorIdAndIsActive(Long professorId, boolean isActive);

    // ==========================================
    // 3. Automation & System Health (The "Smart" features)
    // ==========================================

    // CRITICAL: Used by a Scheduled Task (Cron Job) to auto-terminate exams
    // that have passed their deadline but are still marked 'Active'.
    List<ExamSession> findByIsActiveTrueAndEndTimeBefore(LocalDateTime now);

    // "Global Watch": Find all exams currently in progress across the system.
    // Useful for a "Super Admin" dashboard or load balancing.
    @Query("SELECT e FROM ExamSession e WHERE e.isActive = true AND :now BETWEEN e.startTime AND e.endTime")
    List<ExamSession> findLiveExams(@Param("now") LocalDateTime now);

    // ==========================================
    // 4. Feature Specific
    // ==========================================
    // Analytics: Find all exams that are strictly enforcing Mobile Sentinel
    List<ExamSession> findByIsMobileSentinelActiveTrue();
}