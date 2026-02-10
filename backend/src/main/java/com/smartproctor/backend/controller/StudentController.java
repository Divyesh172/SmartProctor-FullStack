package com.smartproctor.backend.controller;

import com.smartproctor.backend.dto.ErrorResponse;
import com.smartproctor.backend.dto.RegisterStudentRequest;
import com.smartproctor.backend.dto.StudentResponse;
import com.smartproctor.backend.model.Student.ExamStatus;
import com.smartproctor.backend.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final StudentService studentService;

    // ==========================================
    // 1. Onboarding (The Lobby)
    // ==========================================
    @PostMapping("/join")
    public ResponseEntity<StudentResponse> joinExam(
            @Valid @RequestBody RegisterStudentRequest request,
            HttpServletRequest servletRequest) {

        // Security: Capture the real IP from the request header (if behind proxy) or remote address
        String realIp = servletRequest.getHeader("X-Forwarded-For");
        if (realIp == null || realIp.isEmpty()) {
            realIp = servletRequest.getRemoteAddr();
        }

        // Inject IP into the DTO for the Service to use
        request.setIpAddress(realIp);

        log.info("Student joining from IP: {}", realIp);
        StudentResponse response = studentService.joinExam(request);
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // 2. Student Dashboard
    // ==========================================
    @GetMapping("/{studentId}")
    public ResponseEntity<StudentResponse> getStudentDetails(@PathVariable Long studentId) {
        return ResponseEntity.ok(studentService.getStudentById(studentId));
    }

    // ==========================================
    // 3. Status Polling (The "Am I Banned?" Check)
    // ==========================================
    /**
     * Frontend calls this every 5-10 seconds.
     * 1. Updates "Last Seen" (Heartbeat).
     * 2. Returns current status (e.g., if Prof clicked "Terminate", frontend sees it here).
     */
    @GetMapping("/{studentId}/status")
    public ResponseEntity<Map<String, Object>> checkStatus(@PathVariable Long studentId) {
        ExamStatus status = studentService.checkStatus(studentId);

        // Return a lightweight object to save bandwidth
        return ResponseEntity.ok(Map.of(
            "studentId", studentId,
            "status", status,
            "timestamp", LocalDateTime.now()
        ));
    }

    /**
     * Updates the heartbeat without returning full status (Optional optimization).
     * Useful for very large exams to reduce DB load.
     */
    @PostMapping("/{studentId}/heartbeat")
    public ResponseEntity<Void> sendHeartbeat(@PathVariable Long studentId) {
        // You might want to add a specific method in StudentService like 'updateHeartbeat(id)'
        // For now, checking status implicitly updates activity if you modify the service slightly.
        // Or simply:
        studentService.updateLastActivity(studentId);
        return ResponseEntity.ok().build();
    }
}