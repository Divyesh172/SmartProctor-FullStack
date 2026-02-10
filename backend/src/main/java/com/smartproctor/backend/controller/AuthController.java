package com.smartproctor.backend.controller;

import com.smartproctor.backend.dto.JwtAuthResponse;
import com.smartproctor.backend.dto.LoginRequest;
import com.smartproctor.backend.dto.RegisterProfessorRequest;
import com.smartproctor.backend.model.Professor;
import com.smartproctor.backend.security.JwtTokenProvider;
import com.smartproctor.backend.service.ProfessorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final ProfessorService professorService;
    private final JwtTokenProvider jwtTokenProvider; // You need this utility class

    // ==========================================
    // 1. Registration (Sign Up)
    // ==========================================
    @PostMapping("/register")
    public ResponseEntity<Professor> registerProfessor(@Valid @RequestBody RegisterProfessorRequest request) {
        log.info("Registering new professor: {}", request.getEmail());
        Professor professor = professorService.registerProfessor(request);
        return ResponseEntity.ok(professor);
    }

    // ==========================================
    // 2. Authentication (Login)
    // ==========================================
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmail());

        // Step 1: Validate Credentials via Service
        // This checks Password + isVerified status
        Professor professor = professorService.login(request);

        // Step 2: Generate Token
        // We embed the Email and Role into the token claims
        String token = jwtTokenProvider.generateToken(professor);

        // Step 3: Return the Payload
        JwtAuthResponse response = new JwtAuthResponse(
                token,
                professor.getId(),
                professor.getEmail(),
                professor.getRole().name()
        );

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // 3. Session Management (Persistence)
    // ==========================================
    /**
     * Called by React on page load (useEffect) to check if the user is still logged in.
     */
    @GetMapping("/me")
        public ResponseEntity<JwtAuthResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
            Professor professor = professorService.getProfessorByEmail(userDetails.getUsername());

            // Re-issue token (optional, for sliding expiration) or just return info
            String token = jwtTokenProvider.generateToken(professor);

            return ResponseEntity.ok(new JwtAuthResponse(
                    token,
                    professor.getId(),
                    professor.getEmail(),
                    professor.getRole().name()
            ));
        }

    // ==========================================
    // 4. Admin Operations (The Gatekeeper)
    // ==========================================

    // Endpoint for the "Super Admin" to approve pending professors
    @PutMapping("/approve/{professorId}")
    // @PreAuthorize("hasRole('ADMIN')") // Uncomment if you have an Admin user
    public ResponseEntity<Void> approveProfessor(@PathVariable Long professorId) {
        professorService.approveProfessor(professorId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Professor>> getPendingProfessors() {
        return ResponseEntity.ok(professorService.getUnverifiedProfessors());
    }
}