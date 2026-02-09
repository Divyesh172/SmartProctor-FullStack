package com.smartproctor.backend.controller;

import com.smartproctor.backend.dto.CheatReportDTO;
import com.smartproctor.backend.model.ExamSession;
import com.smartproctor.backend.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam")
@CrossOrigin(origins = "*") // Allows React and Go to talk to this
public class ExamController {

    @Autowired
    private ExamService examService;

    // --- EXISTING ENDPOINTS ---
    @PostMapping("/create")
    public ResponseEntity<ExamSession> createExam(@RequestBody ExamSession exam) {
        return ResponseEntity.ok(examService.createExam(exam));
    }

    @GetMapping("/active")
    public ResponseEntity<List<ExamSession>> getActiveExams() {
        return ResponseEntity.ok(examService.getActiveExams());
    }

    // --- NEW ENDPOINT FOR GO ---
    @PostMapping("/report-cheat")
    public ResponseEntity<String> reportCheat(@RequestBody CheatReportDTO report) {
        if (report.getSession_id() == null || report.getReason() == null) {
            return ResponseEntity.badRequest().body("Invalid Report Data");
        }
        
        examService.logCheatIncident(report);
        return ResponseEntity.ok("Incident Logged Successfully");
    }
}