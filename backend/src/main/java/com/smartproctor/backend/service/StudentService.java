package com.smartproctor.backend.service;

import com.smartproctor.backend.dto.RegisterStudentRequest;
import com.smartproctor.backend.dto.StudentResponse;
import com.smartproctor.backend.exception.ResourceNotFoundException;
import com.smartproctor.backend.model.ExamSession;
import com.smartproctor.backend.model.Student;
import com.smartproctor.backend.model.Student.ExamStatus;
import com.smartproctor.backend.repository.ExamSessionRepository;
import com.smartproctor.backend.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;
    private final ExamSessionRepository examSessionRepository;

    // ==========================================
    // 1. Student Onboarding (The "Handshake")
    // ==========================================
    @Transactional
    public StudentResponse joinExam(RegisterStudentRequest request) {
        log.info("Student attempting to join exam: {} | Email: {}", request.getExamCode(), request.getEmail());

        // Step 1: Find the Exam
        ExamSession exam = examSessionRepository.findByExamCode(request.getExamCode())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid Exam Code: " + request.getExamCode()));

        // Step 2: Check for Double Entry (Security)
        if (studentRepository.existsByEmailAndExamSession(request.getEmail(), exam)) {
            // In a real app, you might return the existing session or throw an error.
            // For now, we throw an error to prevent "tab spamming".
            throw new IllegalArgumentException("You have already joined this exam session.");
        }

        // Step 3: Check if Exam is Open
        if (!exam.isActive() || (exam.getEndTime() != null && LocalDateTime.now().isAfter(exam.getEndTime()))) {
            throw new IllegalStateException("This exam session is closed.");
        }

        // Step 4: Create the Student Profile
        Student student = Student.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .examSession(exam)
                .status(ExamStatus.REGISTERED)
                .strikeCount(0)
                .suspicionScore(0.0)
                // Capture Device Forensics
                .ipAddress(request.getIpAddress()) // Passed from Controller
                .browserFingerprint(request.getBrowserFingerprint())
                // Generate the Unique QR Code for Mobile Sentinel
                .mobilePairingCode(generateUniquePairingCode())
                .isMobileConnected(false)
                .build();

        Student savedStudent = studentRepository.save(student);
        log.info("Student registered successfully: ID {}", savedStudent.getId());

        return mapToResponse(savedStudent);
    }

    // ==========================================
    // 2. Mobile Sentinel Logic (The Unique Feature)
    // ==========================================
    /**
     * Called when the Go Engine detects a valid QR scan.
     * It flips the switch to tell the React Frontend "Phone Connected!"
     */
    @Transactional
    public boolean verifyMobilePairing(String pairingCode) {
        Student student = studentRepository.findByMobilePairingCode(pairingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid Pairing Code"));

        if (student.isMobileConnected()) {
            log.warn("Duplicate mobile connection attempt for student: {}", student.getEmail());
            return true; // Already connected
        }

        student.setMobileConnected(true);
        studentRepository.save(student);
        log.info("Mobile Sentinel Activated for student: {}", student.getEmail());
        return true;
    }

    // ==========================================
    // 3. Status & Polling
    // ==========================================
    public StudentResponse getStudentById(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return mapToResponse(student);
    }

    /**
     * Used by the frontend to check "Am I still allowed to write?"
     * This allows the server to cut the connection instantly if banned.
     */
    public ExamStatus checkStatus(Long studentId) {
        return studentRepository.findById(studentId)
                .map(Student::getStatus)
                .orElse(ExamStatus.TERMINATED);
    }

    // ==========================================
    // 4. Security Actions
    // ==========================================
    @Transactional
    public void submitExam(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        student.setStatus(ExamStatus.SUBMITTED);
        student.setLastActivityAt(LocalDateTime.now());
        studentRepository.save(student);
        log.info("Student {} submitted exam successfully.", studentId);
    }

    @Transactional
    public void updateLastActivity(Long studentId) {
        studentRepository.findById(studentId)
            .ifPresent(student -> student.setLastActivityAt(LocalDateTime.now()));
    }

    // ==========================================
    // Helper Methods
    // ==========================================
    private String generateUniquePairingCode() {
        // Generates a short, readable code like "A7B2-9X" instead of a long UUID
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private StudentResponse mapToResponse(Student s) {
        return StudentResponse.builder()
                .id(s.getId())
                .fullName(s.getFullName())
                .email(s.getEmail())
                .examCode(s.getExamSession().getExamCode())
                .mobilePairingCode(s.getMobilePairingCode()) // Crucial for QR generation
                .isMobileConnected(s.isMobileConnected())
                .status(s.getStatus().name())
                .build();
    }
}