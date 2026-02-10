package com.smartproctor.backend.controller;

import com.smartproctor.backend.dto.CheatReportDTO;
import com.smartproctor.backend.dto.ErrorResponse;
import com.smartproctor.backend.model.CheatIncident;
import com.smartproctor.backend.service.CheatService;
import com.smartproctor.backend.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/proctor")
@RequiredArgsConstructor
@Slf4j
public class ProctorController {

    private final CheatService cheatService;
    private final StudentService studentService;

    // ==========================================
    // 1. AI Reporting (Python Engine)
    // ==========================================
    /**
     * Receives cheat flags from the Python Computer Vision script.
     * Secured by API Key (handled by SecurityConfig + ApiKeyAuthFilter).
     */
    @PostMapping("/report")
    public ResponseEntity<?> reportIncident(@Valid @RequestBody CheatReportDTO report) {
        log.debug("Received cheat report for Student ID: {} | Type: {}", report.getStudentId(), report.getCheatType());

        try {
            CheatIncident incident = cheatService.logIncident(report);

            if (incident == null) {
                // This happens if the incident was throttled (duplicate alert)
                return ResponseEntity.status(HttpStatus.OK).body("Incident acknowledged but throttled.");
            }

            return ResponseEntity.ok(Map.of(
                "status", "logged",
                "incidentId", incident.getId(),
                "newSuspicionScore", incident.getStudent().getSuspicionScore()
            ));

        } catch (Exception e) {
            log.error("Failed to log incident", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Incident Logging Failed",
                    e.getMessage(),
                    "/api/proctor/report"
            ));
        }
    }

    // ==========================================
    // 2. Mobile Sentinel (Go Engine)
    // ==========================================
    /**
     * Called by the Go WebSocket Engine when a phone successfully scans the QR code.
     * This triggers the "Green Checkmark" on the Student's screen.
     */
    @PostMapping("/mobile/handshake")
    public ResponseEntity<?> verifyMobileConnection(@RequestBody Map<String, String> payload) {
        String pairingCode = payload.get("pairingCode");

        if (pairingCode == null || pairingCode.isBlank()) {
            return ResponseEntity.badRequest().body("Pairing code is required");
        }

        log.info("Received Mobile Handshake for code: {}", pairingCode);

        boolean isConnected = studentService.verifyMobilePairing(pairingCode);

        if (isConnected) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Mobile device paired successfully"
            ));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", "Invalid pairing code"
            ));
        }
    }

    // ==========================================
    // 3. System Health Check
    // ==========================================
    /**
     * Used by the Python script at startup to verify it can talk to the server.
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Proctor Server is UP. Ready to receive evidence.");
    }
}