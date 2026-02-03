package com.smartproctor.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartproctor.backend.model.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
	// Magic Method: Spring reads "findByEmail" and writes the SQL for you.
	Optional<Student> findByEmail(String email);
}
