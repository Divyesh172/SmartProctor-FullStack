package com.smartproctor.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartproctor.backend.model.ExamSession;

@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession, Long>{
	// Magic Method: Finds the exam by its unique join code (e.g., "AI_2026")
	Optional<ExamSession> findByExamCode(String examCode);
}
