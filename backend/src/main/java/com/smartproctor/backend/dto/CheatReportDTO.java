package com.smartproctor.backend.dto;

public class CheatReportDTO {
    private String session_id; // Corresponds to Exam Code
    private String reason;     // e.g., "LOOKING_AWAY"
    private String timestamp;
    private String confidence;

    // Default Constructor
    public CheatReportDTO() {}

    // Getters and Setters
    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }
}