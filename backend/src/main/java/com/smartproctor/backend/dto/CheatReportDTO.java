package com.smartproctor.backend.dto;

import com.smartproctor.backend.model.CheatIncident.CheatType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheatReportDTO {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Cheat Type is required")
    private CheatType cheatType; // e.g., MOBILE_PHONE_DETECTED

    private String description; // "User looked away for 5 seconds"

    private double confidenceScore; // 0.85

    // The Python script uploads the image to Cloudinary/S3 first,
    // then sends the URL here.
    private String snapshotUrl;
}