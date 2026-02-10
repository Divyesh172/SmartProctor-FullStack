package com.smartproctor.backend.service;

import com.smartproctor.backend.dto.ExamTranscriptDTO;
import com.smartproctor.backend.exception.ResourceNotFoundException;
import com.smartproctor.backend.exception.UnauthorizedException;
import com.smartproctor.backend.model.Question;
import com.smartproctor.backend.model.Student;
import com.smartproctor.backend.model.StudentAnswer;
import com.smartproctor.backend.repository.QuestionRepository;
import com.smartproctor.backend.repository.StudentAnswerRepository;
import com.smartproctor.backend.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentAnswerService {

    private final StudentAnswerRepository studentAnswerRepository;
    private final QuestionRepository questionRepository;
    private final StudentRepository studentRepository;

    // ==========================================
    // 1. Core Exam Taking (The "Auto-Grader")
    // ==========================================
    /**
     * Saves an answer and instantly grades it.
     * Upsert Logic: Updates if the student changes their mind.
     */
    @Transactional
    public void submitAnswer(Long studentId, Long questionId, String selectedOption) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        // Validation: Ensure this question belongs to the student's exam
        if (!question.getExamSession().getId().equals(student.getExamSession().getId())) {
             throw new UnauthorizedException("This question is not part of your exam.");
        }

        StudentAnswer answer = studentAnswerRepository.findByStudentIdAndQuestionId(studentId, questionId)
                .orElse(new StudentAnswer());

        // Set Relationships
        if (answer.getId() == null) { // New answer
            answer.setStudent(student);
            answer.setQuestion(question);
        }

        answer.setSelectedOption(selectedOption);

        // AUTO-GRADING LOGIC
        // We compare ensuring case-insensitivity (A vs a)
        boolean isCorrect = selectedOption.equalsIgnoreCase(question.getCorrectOption());
        answer.setCorrect(isCorrect);

        studentAnswerRepository.save(answer);
    }

    // ==========================================
    // 2. Scoring & Results
    // ==========================================
    public int calculateScore(Long studentId) {
        List<StudentAnswer> answers = studentAnswerRepository.findByStudentId(studentId);
        return answers.stream()
                .filter(StudentAnswer::isCorrect)
                .mapToInt(a -> a.getQuestion().getMarks())
                .sum();
    }

    // ==========================================
    // 3. Advanced Reporting (The "Unique" Feature)
    // ==========================================
    /**
     * Generates a detailed breakdown of the exam performance.
     * Useful for the "View Results" page.
     */
    public ExamTranscriptDTO generateTranscript(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        List<StudentAnswer> answers = studentAnswerRepository.findByStudentId(studentId);
        int totalScore = calculateScore(studentId);
        int maxPossibleScore = answers.stream().mapToInt(a -> a.getQuestion().getMarks()).sum();
        // Note: Ideally maxScore should come from all questions in the exam, not just answered ones.

        return ExamTranscriptDTO.builder()
                .studentName(student.getFullName())
                .examTitle(student.getExamSession().getTitle())
                .totalScore(totalScore)
                .answeredCount(answers.size())
                .correctCount((int) answers.stream().filter(StudentAnswer::isCorrect).count())
                .build();
    }
}