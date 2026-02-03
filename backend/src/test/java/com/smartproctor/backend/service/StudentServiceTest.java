package com.smartproctor.backend.service;

import com.smartproctor.backend.model.ExamSession;
import com.smartproctor.backend.model.Student;
import com.smartproctor.backend.repository.ExamSessionRepository;
import com.smartproctor.backend.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ExamSessionRepository examSessionRepository;

    @InjectMocks
    private StudentService studentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterStudent_Success() {
        // GIVEN
        String examCode = "TEST_101";
        ExamSession mockExam = new ExamSession();
        mockExam.setExamCode(examCode);
        mockExam.setActive(true);

        Student mockStudent = new Student();
        mockStudent.setId(1L);
        mockStudent.setFullName("Test User");
        mockStudent.setExamSession(mockExam);

        when(examSessionRepository.findByExamCode(examCode)).thenReturn(Optional.of(mockExam));
        when(studentRepository.save(any(Student.class))).thenReturn(mockStudent);

        // WHEN
        Student result = studentService.registerStudent("Test User", "test@email.com", examCode);

        // THEN
        assertNotNull(result);
        assertEquals("Test User", result.getFullName());
    }

    @Test
    void testAddStrike_TriggersBan() {
        // GIVEN
        Long studentId = 1L;
        Student student = new Student();
        student.setId(studentId);
        student.setStrikeCount(2); 
        student.setBanned(false);
        
        ExamSession mockExam = new ExamSession(); 
        mockExam.setExamCode("TEST_101");
        student.setExamSession(mockExam);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

        // WHEN
        int newStrikes = studentService.addStrike(studentId);

        // THEN
        assertEquals(3, newStrikes); 
        assertTrue(student.isBanned()); 
    }
}