package com.smartproctor.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamTranscriptDTO {

    private String studentName;
    private String examTitle;

    private int totalScore;    // The student's score
    private int maxPossibleScore; // The total marks of the exam

    private int answeredCount; // How many questions they attempted
    private int correctCount;  // How many they got right

    private String grade;      // "A", "B", "Fail" (Optional Logic)
}