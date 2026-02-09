package com.smartproctor.backend.service;

import com.smartproctor.backend.dto.CheatReportDTO;
import com.smartproctor.backend.model.CheatIncident;
import com.smartproctor.backend.model.ExamSession;
import com.smartproctor.backend.repository.CheatIncidentRepository;
import com.smartproctor.backend.repository.ExamSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExamService {

    @Autowired
    private ExamSessionRepository examRepository;

    @Autowired
    private CheatIncidentRepository incidentRepository;

    // --- EXISTING METHODS (Do not delete these) ---
    public ExamSession createExam(ExamSession exam) {
        return examRepository.save(exam);
    }

    public List<ExamSession> getAllExams() {
        return examRepository.findAll();
    }
    
    public List<ExamSession> getActiveExams() {
        return examRepository.findByIsActiveTrue();
    }

    // --- NEW METHOD FOR GO ENGINE ---
    public void logCheatIncident(CheatReportDTO report) {
        System.out.println("⚠️ VIOLATION RECEIVED FROM GO: " + report.getReason());

        CheatIncident incident = new CheatIncident(
                report.getSession_id(),
                report.getReason(),
                LocalDateTime.now(), 
                report.getConfidence()
        );

        incidentRepository.save(incident);
    }
}