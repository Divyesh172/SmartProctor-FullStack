package com.smartproctor.backend.service;

import com.smartproctor.backend.dto.CheatReportDTO;
import com.smartproctor.backend.exception.ResourceNotFoundException;
import com.smartproctor.backend.model.CheatIncident;
import com.smartproctor.backend.model.CheatIncident.CheatType;
import com.smartproctor.backend.model.CheatIncident.IncidentStatus;
import com.smartproctor.backend.model.ExamSession;
import com.smartproctor.backend.model.Student;
import com.smartproctor.backend.repository.CheatIncidentRepository;
import com.smartproctor.backend.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheatService {

    private final CheatIncidentRepository cheatIncidentRepository;
    private final StudentRepository studentRepository;

    // ==========================================
    // 1. Core Logic: Processing Incoming Alerts
    // ==========================================
    /**
     * The main entry point for the Python AI and Go Engine.
     */
    @Transactional
    public CheatIncident logIncident(CheatReportDTO report) {
        Long studentId = report.getStudentId();
        CheatType type = report.getCheatType();

        // Step 1: Throttling (The "Anti-Spam" Filter)
        // If we just flagged this student for this specific thing <10s ago, ignore it.
        if (cheatIncidentRepository.existsRecentIncident(studentId, type)) {
            log.debug("Skipping duplicate incident for student {}: {}", studentId, type);
            return null;
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        ExamSession exam = student.getExamSession();
        if (!exam.isActive()) {
             log.warn("Ignored cheat report for closed exam: {}", exam.getExamCode());
             return null;
        }

        // Step 2: Calculate Impact
        double suspicionDelta = calculateSuspicionImpact(type, report.getConfidenceScore());

        // Step 3: Update Student Record (Real-time Punishment)
        student.setSuspicionScore(student.getSuspicionScore() + suspicionDelta);
        student.setStrikeCount(student.getStrikeCount() + 1); // Increment raw count

        // Auto-Ban Logic (Optional: Can be toggled)
        if (exam.getMaxWarnings() > 0 && student.getStrikeCount() > exam.getMaxWarnings()) {
             // You could auto-terminate here, but usually, it's better to let the Prof decide.
             // We just flag them as "High Risk" via the score.
        }
        studentRepository.save(student);

        // Step 4: Create the Evidence Record
        CheatIncident incident = CheatIncident.builder()
                .student(student)
                .examSession(exam)
                .cheatType(type)
                .description(report.getDescription())
                .confidenceScore(report.getConfidenceScore())
                .snapshotUrl(report.getSnapshotUrl()) // The URL from Cloudinary/S3
                .status(IncidentStatus.PENDING_REVIEW)
                .build();

        log.info("Cheat Incident Logged: Student {} | Type {} | Score +{}", student.getEmail(), type, suspicionDelta);
        return cheatIncidentRepository.save(incident);
    }

    // ==========================================
    // 2. Professor Actions (Review & Adjudication)
    // ==========================================
    /**
     * Allows the professor to mark an incident as FALSE_POSITIVE or VERIFIED.
     * Handles the "Refund" logic if false positive.
     */
    @Transactional
    public CheatIncident reviewIncident(Long incidentId, IncidentStatus newStatus) {
        CheatIncident incident = cheatIncidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));

        IncidentStatus oldStatus = incident.getStatus();

        // Logic: If marking as FALSE_POSITIVE, we must "refund" the damage done to the student.
        if (newStatus == IncidentStatus.FALSE_POSITIVE && oldStatus != IncidentStatus.FALSE_POSITIVE) {
            revertStudentDamage(incident);
        }
        // Logic: If reverting FROM False Positive back to Verified (oops case)
        else if (oldStatus == IncidentStatus.FALSE_POSITIVE && newStatus == IncidentStatus.VERIFIED) {
            applyStudentDamage(incident);
        }

        incident.setStatus(newStatus);
        return cheatIncidentRepository.save(incident);
    }

    // ==========================================
    // 3. Analytics & Dashboard Data
    // ==========================================
    public Map<String, Object> getExamCheatSummary(Long examId) {
        Map<String, Object> summary = new HashMap<>();

        // Pie Chart Data
        Map<String, Long> typeBreakdown = new HashMap<>();
        for (CheatType type : CheatType.values()) {
            long count = cheatIncidentRepository.countByExamSessionIdAndCheatType(examId, type);
            if (count > 0) typeBreakdown.put(type.name(), count);
        }
        summary.put("breakdown", typeBreakdown);

        // Top Offenders List
        List<Object[]> topCheaters = cheatIncidentRepository.findTopCheaters(examId);
        // Limit to top 5 for the summary widget
        summary.put("topOffenders", topCheaters.stream().limit(5).toList());

        return summary;
    }

    public List<CheatIncident> getIncidentsForExam(Long examId) {
        return cheatIncidentRepository.findByExamSessionIdOrderByDetectedAtDesc(examId);
    }

    public List<CheatIncident> getIncidentsForStudent(Long studentId) {
        return cheatIncidentRepository.findByStudentIdOrderByDetectedAtDesc(studentId);
    }

    // ==========================================
    // Helper Methods (The "Judge" Logic)
    // ==========================================
    private double calculateSuspicionImpact(CheatType type, double confidence) {
        double baseScore;
        switch (type) {
            case MOBILE_PHONE_DETECTED:
            case MULTIPLE_FACES_DETECTED:
                baseScore = 20.0; // Major Offense
                break;
            case SUSPICIOUS_AUDIO:
            case COPY_PASTE_DETECTED:
                baseScore = 10.0; // Medium Offense
                break;
            case LOOKING_AWAY:
            case NO_FACE_DETECTED:
            case TAB_SWITCH:
            default:
                baseScore = 5.0;  // Minor Offense
                break;
        }
        // Scale by AI confidence (e.g., if AI is only 50% sure, halve the penalty)
        return baseScore * (confidence > 0.5 ? confidence : 0.5);
    }

    private void revertStudentDamage(CheatIncident incident) {
        Student student = incident.getStudent();
        double refund = calculateSuspicionImpact(incident.getCheatType(), incident.getConfidenceScore());

        student.setSuspicionScore(Math.max(0, student.getSuspicionScore() - refund));
        student.setStrikeCount(Math.max(0, student.getStrikeCount() - 1));

        studentRepository.save(student);
        log.info("Reverted penalty for student {}: -{} score", student.getId(), refund);
    }

    private void applyStudentDamage(CheatIncident incident) {
        Student student = incident.getStudent();
        double penalty = calculateSuspicionImpact(incident.getCheatType(), incident.getConfidenceScore());

        student.setSuspicionScore(student.getSuspicionScore() + penalty);
        student.setStrikeCount(student.getStrikeCount() + 1);

        studentRepository.save(student);
    }
}