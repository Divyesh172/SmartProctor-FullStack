package com.smartproctor.backend.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "exam_sessions")
public class ExamSession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String subjectName; // e.g., "Artificial Intelligence"
	
	@Column(unique = true) // Logic: No two exams can have same join code
	private String examCode;
	
	//The Time Boundaries
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	
	// The State of the Room (Open or Closed)
	private boolean isActive = true;
	
	@OneToMany(mappedBy = "examSession", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Student> students = new ArrayList<>();
}
