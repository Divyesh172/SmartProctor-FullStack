package com.smartproctor.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmartProctorApplication {
	public static void main(String[] args) {
		SpringApplication.run(SmartProctorApplication.class, args);
		System.out.println("--- Smart Proctor Backend is Alive ---");
	}
}