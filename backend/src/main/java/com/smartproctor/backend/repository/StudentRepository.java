package com.smartproctor.backend.repository;

import com.smartproctor.backend.model.ExamSession;
import com.smartproctor.backend.model.Student;
import com.smartproctor.backend.model.Student.ExamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // ==========================================
    // 1. Core Identification
    // ==========================================
    Optional<Student> findByEmail(String email);

    // Used to validate login/join requests
    boolean existsByEmailAndExamSession(String email, ExamSession examSession);

    // ==========================================
    // 2. Mobile Sentinel (The Unique Feature)
    // ==========================================
    // CRITICAL: This connects the QR Code scan to the Student record.
    // The Go Engine will query this to verify the phone connection.
    Optional<Student> findByMobilePairingCode(String mobilePairingCode);

    // ==========================================
    // 3. Professor Dashboard & Live Monitoring
    // ==========================================
    // Get all students for a specific exam
    List<Student> findByExamSessionId(Long examSessionId);

    // "Who is currently writing?" (Live View)
    List<Student> findByExamSessionIdAndStatus(Long examSessionId, ExamStatus status);

    // "Show me the cheaters" (Risk Analysis)
    // Fetches students with high suspicion OR high strike counts, ordered by risk.
    @Query("SELECT s FROM Student s WHERE s.examSession.id = :examId AND (s.suspicionScore > :threshold OR s.strikeCount > 0) ORDER BY s.suspicionScore DESC")
    List<Student> findHighRiskStudents(@Param("examId") Long examId, @Param("threshold") double threshold);

    // ==========================================
    // 4. Security & Forensics
    // ==========================================
    // Find potential "imposters" (e.g., same IP address for multiple students in one exam)
    @Query("SELECT s FROM Student s WHERE s.examSession.id = :examId AND s.ipAddress IN (SELECT s2.ipAddress FROM Student s2 WHERE s2.examSession.id = :examId GROUP BY s2.ipAddress HAVING COUNT(s2) > 1)")
    List<Student> findStudentsWithSharedIp(@Param("examId") Long examId);

    // ==========================================
    // 5. Atomic Updates (Performance)
    // ==========================================
    // Increment strike count efficiently without fetching the whole object
    @Modifying
    @Query("UPDATE Student s SET s.strikeCount = s.strikeCount + 1, s.suspicionScore = s.suspicionScore + :scoreDelta WHERE s.id = :studentId")
    void incrementSuspicion(@Param("studentId") Long studentId, @Param("scoreDelta") double scoreDelta);
}