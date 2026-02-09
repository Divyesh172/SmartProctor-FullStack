package com.smartproctor.backend.repository;

import com.smartproctor.backend.model.CheatIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CheatIncidentRepository extends JpaRepository<CheatIncident, Long> {
    List<CheatIncident> findByExamCode(String examCode);
}