package com.smartproctor.backend.repository;

import com.smartproctor.backend.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // ==========================================
    // 1. Fetching the Paper
    // ==========================================
    // "Get me the test paper for Exam ID 10"
    List<Question> findByExamSessionId(Long examSessionId);

    // ==========================================
    // 2. Maintenance
    // ==========================================
    // Clean up when an exam is deleted
    void deleteByExamSessionId(Long examSessionId);
}