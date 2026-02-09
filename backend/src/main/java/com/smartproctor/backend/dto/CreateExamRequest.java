package com.smartproctor.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateExamRequest {
	@NotBlank(message = "Subject cannot be empty")
	private String subject;
	
	@NotBlank(message = "Exam Code is required")
	private String examCode;
}
