package com.smartproctor.backend;

import com.smartproctor.backend.model.Professor;
import com.smartproctor.backend.repository.ProfessorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling // Activates the background job to close expired exams
public class SmartProctorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartProctorApplication.class, args);
    }

    /**
     * AUTO-SEEDER:
     * This runs automatically when the server starts.
     * It checks if the database is empty. If yes, it creates a default Admin account.
     * This saves you from manually inserting SQL rows to get started.
     */
    @Bean
    public CommandLineRunner initData(ProfessorRepository professorRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@smartproctor.com";

            if (professorRepository.findByEmail(adminEmail).isEmpty()) {
                Professor admin = Professor.builder()
                        .fullName("System Administrator")
                        .email(adminEmail)
                        .password(passwordEncoder.encode("admin123")) // Default Password
                        .employeeId("ADMIN-001")
                        .department("IT Security")
                        .universityName("Smart Proctor HQ")
                        .role(Professor.Role.ADMIN)
                        .isVerified(true) // Auto-verified
                        .build();

                professorRepository.save(admin);
                System.out.println("✅ DEFAULT ADMIN ACCOUNT CREATED: admin@smartproctor.com / admin123");
            }
        };
    }
}