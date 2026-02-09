package com.smartproctor.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "students")
public class Student {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String fullName;
	
	private String email;
	
	// The Reference "Image" we compare against
	private String profileImageUrl;
	//The Record
	private int strikeCount = 0;
	
	private boolean isBanned = false;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exam_session_id", nullable = false) // Every student MUST HAVE AN exam
	@JsonIgnore // CRITICAL : Prevents infinite loops when converting to JSON
	private ExamSession examSession;
}
