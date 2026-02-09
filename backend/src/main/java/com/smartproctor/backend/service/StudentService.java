package com.smartproctor.backend.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartproctor.backend.dto.StudentResponse;
import com.smartproctor.backend.model.ExamSession;
import com.smartproctor.backend.model.Student;
import com.smartproctor.backend.repository.ExamSessionRepository;
import com.smartproctor.backend.repository.StudentRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StudentService {
	private final StudentRepository studentRepository;
	private final ExamSessionRepository examSessionRepository;
	
	//Dependency Injection: Spring gives us the Repository automatically
	public StudentService(StudentRepository studentRepository, ExamSessionRepository examSessionRepository) {
		this.studentRepository = studentRepository;
		this.examSessionRepository = examSessionRepository;
	}
	
	//Logic: The "The Three Strikes" Rule
	@Transactional //Ensure the database update is safe
	public int addStrike(Long studentId) {
		// 1. Find the student
		Student student = studentRepository.findById(studentId).orElseThrow(() -> new RuntimeException("Student Not Found"));
		
		// 2. Increase strike count
		int newCount = student.getStrikeCount() + 1;
		student.setStrikeCount(newCount);
		
		// 3. Check for Ban Threshold (3 strikes)
		if(newCount >= 3) {
			terminateExam(studentId);
		}
		
		// 4. Save changes
		studentRepository.save(student);
		
		return newCount;
	}
	
	// LOGIC: The "Red Card" (Ban)
	@Transactional
	public void terminateExam(Long studentId) {
		Student student = studentRepository.findById(studentId).orElseThrow(() -> new RuntimeException("Student Not Found"));
		student.setBanned(true);
		log.warn("BAN_TRIGGERED: Student ID {} has been banned from Exam Session {}",
				studentId, student.getExamSession().getExamCode());
		
		studentRepository.save(student);
	}
	
	public Student registerStudent(String name, String email, String examCode) {
		// 1. Validate the exam exists
		ExamSession session = examSessionRepository.findByExamCode(examCode).orElseThrow(() -> new RuntimeException("Exam Code Invalid: " + examCode));
		
		// 2. Validate Exam is actually open (Business Logic)
		if(!session.isActive()) {
			throw new RuntimeException("This exam session is closed.");
		}
		
		// 3. Create and Link
		Student s = new Student();
		s.setFullName(name);
		s.setEmail(email);
		s.setExamSession(session);
		
		return studentRepository.save(s);
	}
	
	public List<StudentResponse> getStudentsByExam(String examCode){
		// 1. Find the exam
		ExamSession session = examSessionRepository.findByExamCode(examCode)
				.orElseThrow(() -> new RuntimeException("Exam not found"));
		// 2. Get students and convert to DTO
		return session.getStudents().stream().map(student -> {
			StudentResponse dto = new StudentResponse();
			dto.setId(student.getId());
			dto.setName(student.getFullName());
			dto.setEmail(student.getEmail());
			dto.setStrikeCount(student.getStrikeCount());
			dto.setBanned(student.isBanned());
			return dto;
		}).collect(Collectors.toList());
	}
	
	public StudentResponse getStudentStatus(Long studentId) {
		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new RuntimeException("Student not found"));
		StudentResponse dto = new StudentResponse();
		dto.setId(student.getId());
		dto.setName(student.getFullName());
		dto.setStrikeCount(student.getStrikeCount());
		dto.setBanned(student.isBanned());
		return dto;
	}
	
	public List<Map<String, String>> getActiveExams() {
		return examSessionRepository.findAll().stream()
				.filter(ExamSession::isActive)
				.map(exam -> {
					Map<String, String> map = new HashMap<>();
					map.put("code", exam.getExamCode());
					map.put("subject", exam.getSubjectName());
					return map;
				})
				.collect(Collectors.toList());
	}
}
