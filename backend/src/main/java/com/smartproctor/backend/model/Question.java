package com.smartproctor.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String questionText;

    // Options for MCQ
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    @JsonIgnore // Don't send the answer to the frontend! (Security)
    private String correctOption; // e.g., "A", "B"

    private int marks; // e.g., 5 points

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_session_id", nullable = false)
    @JsonIgnore
    private ExamSession examSession;
}