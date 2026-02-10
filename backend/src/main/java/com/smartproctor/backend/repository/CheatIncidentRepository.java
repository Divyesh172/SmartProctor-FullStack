package com.smartproctor.backend.repository;

import com.smartproctor.backend.model.CheatIncident;
import com.smartproctor.backend.model.CheatIncident.CheatType;
import com.smartproctor.backend.model.CheatIncident.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheatIncidentRepository extends JpaRepository<CheatIncident, Long> {

    // ==========================================
    // 1. Professor Dashboard (The "Feed")
    // ==========================================
    // Main view: Stream of incoming alerts for a specific exam
    List<CheatIncident> findByExamSessionIdOrderByDetectedAtDesc(Long examSessionId);

    // "To-Do List": Show incidents that still need review (PENDING)
    @Query("SELECT c FROM CheatIncident c WHERE c.examSession.id = :examId AND c.status = 'PENDING_REVIEW'")
    List<CheatIncident> findUnresolvedIncidents(@Param("examId") Long examId);

    // ==========================================
    // 2. Student Specific (The "Rap Sheet")
    // ==========================================
    // Used when clicking on a specific student to see their timeline
    List<CheatIncident> findByStudentIdOrderByDetectedAtDesc(Long studentId);

    // ==========================================
    // 3. Analytics & Charts (The "Wow" Factor)
    // ==========================================
    // "What is the most common method of cheating?" (For Pie Charts)
    Long countByExamSessionIdAndCheatType(Long examSessionId, CheatType cheatType);

    // "Who are my top offenders?" (For High Risk Table)
    // Counts only VERIFIED or PENDING incidents (ignores false positives)
    @Query("SELECT c.student.id, COUNT(c) as total FROM CheatIncident c " +
           "WHERE c.examSession.id = :examId AND c.status != 'FALSE_POSITIVE' " +
           "GROUP BY c.student.id ORDER BY total DESC")
    List<Object[]> findTopCheaters(@Param("examId") Long examId);

    // ==========================================
    // 4. Forensics & Deduplication
    // ==========================================
    // Prevent spam: Check if we just flagged this 2 seconds ago
    // (You will call this from Service layer with a time window)
    @Query("SELECT COUNT(c) > 0 FROM CheatIncident c " +
           "WHERE c.student.id = :studentId AND c.cheatType = :type " +
           "AND c.detectedAt > current_timestamp - 10 SECOND")
    boolean existsRecentIncident(@Param("studentId") Long studentId, @Param("type") CheatType type);
}