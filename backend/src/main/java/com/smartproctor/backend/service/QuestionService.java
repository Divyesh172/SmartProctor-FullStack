package com.smartproctor.backend.service;

import com.smartproctor.backend.dto.QuestionRequest;
import com.smartproctor.backend.exception.ResourceNotFoundException;
import com.smartproctor.backend.exception.UnauthorizedException;
import com.smartproctor.backend.model.*;
import com.smartproctor.backend.repository.ExamSessionRepository;
import com.smartproctor.backend.repository.QuestionRepository;
import com.smartproctor.backend.repository.StudentAnswerRepository;
import com.smartproctor.backend.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final ExamSessionRepository examSessionRepository;
    private final StudentRepository studentRepository;

    // ==========================================
    // 1. Professor Operations (Setting the Paper)
    // ==========================================
    @Transactional
    public Question addQuestion(Long examId, QuestionRequest request, String professorEmail) {
        ExamSession exam = examSessionRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

        // Security: Only the creator can add questions
        if (!exam.getProfessor().getEmail().equals(professorEmail)) {
            throw new UnauthorizedException("You are not authorized to edit this exam.");
        }

        Question question = new Question();
        question.setQuestionText(request.getQuestionText());
        question.setOptionA(request.getOptionA());
        question.setOptionB(request.getOptionB());
        question.setOptionC(request.getOptionC());
        question.setOptionD(request.getOptionD());
        question.setCorrectOption(request.getCorrectOption()); // Stored securely in DB
        question.setMarks(request.getMarks());
        question.setExamSession(exam);

        return questionRepository.save(question);
    }

    // ==========================================
    // 2. Student Operations (Taking the Exam)
    // ==========================================
    /**
     * CRITICAL: Fetches questions but HIDES the correct answer.
     * If you send the raw entity, a smart student can inspect JSON and cheat.
     */
    public List<Question> getQuestionsForStudent(Long examId, Long studentId) {
        // Validation: Is the student actually part of this exam?
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!student.getExamSession().getId().equals(examId)) {
            throw new UnauthorizedException("You are not registered for this exam.");
        }

        List<Question> questions = questionRepository.findByExamSessionId(examId);

        // Security: Scrub the "correctOption" field before sending to frontend
        return questions.stream().map(q -> {
            Question safeQuestion = new Question();
            safeQuestion.setId(q.getId());
            safeQuestion.setQuestionText(q.getQuestionText());
            safeQuestion.setOptionA(q.getOptionA());
            safeQuestion.setOptionB(q.getOptionB());
            safeQuestion.setOptionC(q.getOptionC());
            safeQuestion.setOptionD(q.getOptionD());
            safeQuestion.setMarks(q.getMarks());
            safeQuestion.setCorrectOption(null); // HIDDEN
            return safeQuestion;
        }).collect(Collectors.toList());
    }
}