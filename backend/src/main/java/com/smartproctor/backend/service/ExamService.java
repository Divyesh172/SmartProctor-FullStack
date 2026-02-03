package com.smartproctor.backend.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.smartproctor.backend.model.ExamSession;
import com.smartproctor.backend.repository.ExamSessionRepository;

@Service
public class ExamService {
	private final ExamSessionRepository examSessionRepository;
	
	public ExamService(ExamSessionRepository examSessionRepository) {
		this.examSessionRepository = examSessionRepository;
	}
	
	public ExamSession createSession(String subject, String examCode) {
		ExamSession session = new ExamSession();
		session.setSubjectName(subject);
		session.setExamCode(examCode);
		
		// Auto-set time: Starts NOW, ends in 2 hours
		session.setStartTime(LocalDateTime.now());
		session.setEndTime(LocalDateTime.now().plusHours(2));
		session.setActive(true);
		
		return examSessionRepository.save(session);
	}
}
