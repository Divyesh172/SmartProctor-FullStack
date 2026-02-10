package com.smartproctor.backend.repository;

import com.smartproctor.backend.model.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {

    // ==========================================
    // 1. Core Exam Logic
    // ==========================================
    // "Get all answers for Student X" (Used to calculate final score)
    List<StudentAnswer> findByStudentId(Long studentId);

    // "Did Student X already answer Question Y?"
    // (Prevents them from answering the same question twice if they refresh)
    Optional<StudentAnswer> findByStudentIdAndQuestionId(Long studentId, Long questionId);

    // ==========================================
    // 2. Analytics (The "Smart" Feature)
    // ==========================================
    // "How many students chose Option A for Question 5?"
    // Used to generate a bar chart for the Professor: "Class Performance per Question"
    @Query("SELECT COUNT(sa) FROM StudentAnswer sa WHERE sa.question.id = :questionId AND sa.selectedOption = :option")
    Long countByQuestionIdAndOption(@Param("questionId") Long questionId, @Param("option") String option);

    // "How many students got Question 5 correct?"
    @Query("SELECT COUNT(sa) FROM StudentAnswer sa WHERE sa.question.id = :questionId AND sa.isCorrect = true")
    Long countCorrectAnswers(@Param("questionId") Long questionId);
}