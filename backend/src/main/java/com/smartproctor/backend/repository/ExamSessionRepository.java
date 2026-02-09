package com.smartproctor.backend.repository;

import com.smartproctor.backend.model.ExamSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {

    Optional<ExamSession> findByExamCode(String examCode);

    // --- THIS IS THE MISSING METHOD ---
    List<ExamSession> findByIsActiveTrue();
}