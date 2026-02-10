package com.smartproctor.backend.controller;

import com.smartproctor.backend.dto.*;
import com.smartproctor.backend.model.ExamSession;
import com.smartproctor.backend.model.Question;
import com.smartproctor.backend.service.ExamService;
import com.smartproctor.backend.service.QuestionService;
import com.smartproctor.backend.service.StudentAnswerService;
import com.smartproctor.backend.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@Slf4j
public class ExamController {

    private final ExamService examService;
    private final QuestionService questionService;
    private final StudentAnswerService studentAnswerService;
    private final StudentService studentService;

    // ==========================================
    // 1. Professor Endpoints (Management)
    // ==========================================

    @PostMapping("/create")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ExamSession> createExam(
            @Valid @RequestBody CreateExamRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Creating exam for professor: {}", userDetails.getUsername());
        ExamSession exam = examService.createExam(request, userDetails.getUsername());
        return ResponseEntity.ok(exam);
    }

    @GetMapping("/professor")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<List<ExamSession>> getMyExams(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(examService.getExamsForProfessor(userDetails.getUsername()));
    }

    @GetMapping("/{examId}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ExamSession> getExamDetails(
            @PathVariable Long examId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(examService.getExamById(examId, userDetails.getUsername()));
    }

    @DeleteMapping("/{examId}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Void> deleteExam(
            @PathVariable Long examId,
            @AuthenticationPrincipal UserDetails userDetails) {
        examService.deleteExam(examId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // Toggle Publish (Draft -> Live)
    @PatchMapping("/{examId}/publish")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ExamSession> togglePublish(
            @PathVariable Long examId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(examService.togglePublishStatus(examId, userDetails.getUsername()));
    }

    // Security Feature: Reset the Join Code if it leaks
    @PatchMapping("/{examId}/regenerate-code")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<String> regenerateCode(
            @PathVariable Long examId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(examService.regenerateCode(examId, userDetails.getUsername()));
    }

    // The "Command Center" Stats
    @GetMapping("/{examId}/live-stats")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Map<String, Object>> getLiveStats(@PathVariable Long examId) {
        // Note: We might want to add a security check here to ensure the requester owns the exam
        return ResponseEntity.ok(examService.getLiveExamStats(examId));
    }

    // ==========================================
    // 2. Question Management (Professor)
    // ==========================================

    @PostMapping("/{examId}/questions")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Question> addQuestion(
            @PathVariable Long examId,
            @Valid @RequestBody QuestionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(questionService.addQuestion(examId, request, userDetails.getUsername()));
    }

    // ==========================================
    // 3. Student Endpoints (Taking the Exam)
    // ==========================================

    /**
     * Fetch the Paper.
     * CRITICAL: This endpoint uses `getQuestionsForStudent` which HIDES correct answers.
     */
    @GetMapping("/{examId}/questions/take")
    public ResponseEntity<List<Question>> getQuestionsForStudent(
            @PathVariable Long examId,
            @RequestParam Long studentId) { // Passed from Frontend after Login

        log.info("Student {} fetching questions for Exam {}", studentId, examId);
        return ResponseEntity.ok(questionService.getQuestionsForStudent(examId, studentId));
    }

    /**
     * Submit a single answer (Auto-Save).
     */
    @PostMapping("/{examId}/answers")
    public ResponseEntity<Void> submitAnswer(
            @PathVariable Long examId,
            @RequestParam Long studentId, // We need to know WHO is answering
            @Valid @RequestBody SubmitAnswerRequest request) {

        studentAnswerService.submitAnswer(studentId, request.getQuestionId(), request.getSelectedOption());
        return ResponseEntity.ok().build();
    }

    /**
     * Finish the Exam.
     * Calculates final score and updates status to SUBMITTED.
     */
    @PostMapping("/{examId}/submit")
    public ResponseEntity<ExamTranscriptDTO> submitExam(
            @PathVariable Long examId,
            @RequestParam Long studentId) {

        studentService.submitExam(studentId); // Update status to SUBMITTED
        ExamTranscriptDTO transcript = studentAnswerService.generateTranscript(studentId); // Calculate Score

        return ResponseEntity.ok(transcript);
    }

    /**
     * Get Results (Transcript).
     * Only works if the exam is configured to show results immediately.
     */
    @GetMapping("/{examId}/transcript")
    public ResponseEntity<ExamTranscriptDTO> getTranscript(
            @RequestParam Long studentId) {
        return ResponseEntity.ok(studentAnswerService.generateTranscript(studentId));
    }
}