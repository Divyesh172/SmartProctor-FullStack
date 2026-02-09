package com.smartproctor.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cheat_incidents")
public class CheatIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String examCode;
    private String violationType; 
    private LocalDateTime incidentTime;
    private String confidenceLevel;

    // Constructors
    public CheatIncident() {}

    public CheatIncident(String examCode, String violationType, LocalDateTime incidentTime, String confidenceLevel) {
        this.examCode = examCode;
        this.violationType = violationType;
        this.incidentTime = incidentTime;
        this.confidenceLevel = confidenceLevel;
    }

    // Getters
    public Long getId() { return id; }
    public String getExamCode() { return examCode; }
    public String getViolationType() { return violationType; }
    public LocalDateTime getIncidentTime() { return incidentTime; }
    public String getConfidenceLevel() { return confidenceLevel; }
}