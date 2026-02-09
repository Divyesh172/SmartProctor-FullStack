package com.smartproctor.backend.dto;

import lombok.Data;

@Data
public class StudentResponse {
	private Long id;
	private String name;
	private String email;
	private int StrikeCount;
	private boolean isBanned;
}
