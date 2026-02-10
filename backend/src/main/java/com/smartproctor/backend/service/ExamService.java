package com.smartproctor.backend.service;

import com.smartproctor.backend.dto.CreateExamRequest;
import com.smartproctor.backend.exception.ResourceNotFoundException;
import com.smartproctor.backend.exception.UnauthorizedException;
import com.smartproctor.backend.model.ExamSession;
import com.smartproctor.backend.model.ExamSession.SensitivityLevel;
import com.smartproctor.backend.model.Professor;
import com.smartproctor.backend.model.Student;
import com.smartproctor.backend.repository.ExamSessionRepository;
import com.smartproctor.backend.repository.ProfessorRepository;
import com.smartproctor.backend.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamService {

    private final ExamSessionRepository examSessionRepository;
    private final ProfessorRepository professorRepository;
    private final StudentRepository studentRepository;

    private static final String ALPHANUMERIC = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Excluded I, 1, O, 0 for readability
    private final SecureRandom random = new SecureRandom();

    // ==========================================
    // 1. Exam Creation & Configuration
    // ==========================================
    @Transactional
    public ExamSession createExam(CreateExamRequest request, String professorEmail) {
        Professor professor = professorRepository.findByEmail(professorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Professor not found"));

        // Optional: Check if professor is verified (Admin Gatekeeper)
        if (!professor.isVerified()) {
            throw new UnauthorizedException("Your account is pending verification. You cannot create exams yet.");
        }

        String uniqueCode = generateUniqueExamCode();

        ExamSession exam = ExamSession.builder()
                .title(request.getTitle())
                .subjectCode(request.getSubjectCode())
                .examCode(uniqueCode)
                .instructions(request.getInstructions())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .durationMinutes(request.getDurationMinutes())
                // Smart Features Configuration
                .isMobileSentinelActive(request.isMobileSentinelActive())
                .sensitivity(request.getSensitivity() != null ? request.getSensitivity() : SensitivityLevel.MEDIUM)
                .maxWarnings(request.getMaxWarnings())
                // Defaults
                .professor(professor)
                .isActive(true)
                .isPublished(false) // Draft mode by default
                .build();

        log.info("Exam created: {} by {}", uniqueCode, professorEmail);
        return examSessionRepository.save(exam);
    }

    // ==========================================
    // 2. Professor Dashboard Operations
    // ==========================================
    public List<ExamSession> getExamsForProfessor(String professorEmail) {
        Professor professor = professorRepository.findByEmail(professorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Professor not found"));

        return examSessionRepository.findByProfessorIdOrderByCreatedAtDesc(professor.getId());
    }

    public ExamSession getExamById(Long examId, String professorEmail) {
        ExamSession exam = examSessionRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

        // Security Check: Ensure the requester owns this exam
        if (!exam.getProfessor().getEmail().equals(professorEmail)) {
            throw new UnauthorizedException("You do not have permission to view this exam.");
        }
        return exam;
    }

    @Transactional
    public void deleteExam(Long examId, String professorEmail) {
        ExamSession exam = getExamById(examId, professorEmail); // Reuse security check

        // Logic: Should we allow deleting an exam with student data?
        // Better to archive, but for now we allow delete.
        examSessionRepository.delete(exam);
        log.info("Exam {} deleted by {}", examId, professorEmail);
    }

    // ==========================================
    // 3. Exam Control (Start/Stop/Reset)
    // ==========================================
    @Transactional
    public ExamSession togglePublishStatus(Long examId, String professorEmail) {
        ExamSession exam = getExamById(examId, professorEmail);
        exam.setPublished(!exam.isPublished());
        return examSessionRepository.save(exam);
    }

    @Transactional
    public String regenerateCode(Long examId, String professorEmail) {
        ExamSession exam = getExamById(examId, professorEmail);
        String newCode = generateUniqueExamCode();
        exam.setExamCode(newCode);
        examSessionRepository.save(exam);
        return newCode;
    }

    // ==========================================
    // 4. Live Monitoring & Stats
    // ==========================================
    /**
     * Returns a quick dashboard summary for a specific running exam.
     */
    public Map<String, Object> getLiveExamStats(Long examId) {
        List<Student> students = studentRepository.findByExamSessionId(examId);

        long totalJoined = students.size();
        long activeWriters = students.stream().filter(s -> s.getStatus() == Student.ExamStatus.IN_PROGRESS).count();
        long submitted = students.stream().filter(s -> s.getStatus() == Student.ExamStatus.SUBMITTED).count();
        long banned = students.stream().filter(s -> s.isBanned()).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCandidates", totalJoined);
        stats.put("currentlyWriting", activeWriters);
        stats.put("completed", submitted);
        stats.put("terminated", banned);

        return stats;
    }

    // ==========================================
    // 5. Automation (The "Cron Job" Logic)
    // ==========================================
    /**
     * This method should be called by a Scheduled Task every minute.
     * It finds exams that have passed their EndTime and closes them.
     */
    @Transactional
    public void closeExpiredExams() {
        LocalDateTime now = LocalDateTime.now();
        List<ExamSession> expiredExams = examSessionRepository.findByIsActiveTrueAndEndTimeBefore(now);

        for (ExamSession exam : expiredExams) {
            log.info("Auto-closing expired exam: {}", exam.getExamCode());
            exam.setActive(false);
            // Optional: Auto-submit all students who are still "IN_PROGRESS"
            // This prevents students from losing work if they forgot to click submit.
        }
        examSessionRepository.saveAll(expiredExams);
    }

    // ==========================================
    // Helper Methods
    // ==========================================
    private String generateUniqueExamCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
            }
            code = sb.toString();
        } while (examSessionRepository.existsByExamCode(code)); // Ensure global uniqueness
        return code;
    }
}