package com.smartproctor.backend.repository;

import com.smartproctor.backend.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    // ==========================================
    // 1. Authentication & Onboarding
    // ==========================================
    Optional<Professor> findByEmail(String email);

    // Validation during registration to prevent duplicates
    boolean existsByEmail(String email);
    boolean existsByEmployeeId(String employeeId);

    // ==========================================
    // 2. Admin Dashboard (The "Gatekeeper" Feature)
    // ==========================================
    // Fetch all professors who registered but aren't allowed to create exams yet.
    // Useful for an "Approve Users" screen in your frontend.
    @Query("SELECT p FROM Professor p WHERE p.isVerified = false ORDER BY p.joinedAt DESC")
    List<Professor> findUnverifiedProfessors();

    // Fetch by specific role (e.g., find all HEAD_OF_DEPT users)
    List<Professor> findByRole(Professor.Role role);

    // ==========================================
    // 3. Analytics & Reporting
    // ==========================================
    // "Which department is using the system the most?"
    List<Professor> findByDepartment(String department);

    // Returns a list of departments and how many professors are in each.
    // Great for a "System Usage" chart.
    @Query("SELECT p.department, COUNT(p) FROM Professor p GROUP BY p.department")
    List<Object[]> findDepartmentStats();
}