package com.smartproctor.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartproctor.backend.dto.CreateExamRequest;
import com.smartproctor.backend.dto.RegisterStudentRequest;
import com.smartproctor.backend.dto.StudentResponse;
import com.smartproctor.backend.model.ExamSession;
import com.smartproctor.backend.model.Student;
import com.smartproctor.backend.service.ExamService;
import com.smartproctor.backend.service.StudentService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/exam")
@Slf4j
public class ExamController {
	private final StudentService studentService;
	private final ExamService examService;
	
	public ExamController(StudentService studentService, ExamService examService) {
		this.studentService = studentService;
		this.examService = examService;
	}
	
	@PostMapping("/create")
	public ResponseEntity<String> createExam(@Valid @RequestBody CreateExamRequest request){
		ExamSession session = examService.createSession(request.getSubject(), request.getExamCode());
		return ResponseEntity.ok("Exam Created: " + session.getSubjectName());
	}
	
	@PostMapping("/register")
		public ResponseEntity<String> register(@Valid @RequestBody RegisterStudentRequest request){
				Student savedStudent = studentService.registerStudent(
						request.getName(), 
						request.getEmail(), 
						request.getExamCode()
				);
				return ResponseEntity.ok("Registered ID: " + savedStudent.getId()); 
	}
	
	// URL: POST http://localhost:8080/api/exam/report-cheat
	@PostMapping("/report-cheat")
	public ResponseEntity<String> reportCheat(@RequestParam Long studentId){
		log.info("CHEAT_REPORT: Received signal for ID {}", studentId);
		
		int newStrikes = studentService.addStrike(studentId);
		
		if(newStrikes >= 3) {
			return ResponseEntity.status(403).body("BAN: Exam Terminated.");
		}
		
		return ResponseEntity.ok("WARNING: Strike added. Total: " + newStrikes);
	}
	
	@GetMapping("/list-students")
	public ResponseEntity<List<StudentResponse>> getStudents(@RequestParam String examCode){
		List<StudentResponse> students = studentService.getStudentsByExam(examCode);
		return ResponseEntity.ok(students);
	}
	
	@GetMapping("/status")
	public ResponseEntity<StudentResponse> getStudentStatus(@RequestParam Long studentId){
		StudentResponse status = studentService.getStudentStatus(studentId);
		return ResponseEntity.ok(status);
	}
	
	@GetMapping("/active")
	public ResponseEntity<List<Map<String, String>>> getActiveExams(){
		return ResponseEntity.ok(studentService.getActiveExams());
	}
}
