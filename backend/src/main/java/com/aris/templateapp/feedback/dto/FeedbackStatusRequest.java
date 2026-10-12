package com.aris.templateapp.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record FeedbackStatusRequest(
        @NotBlank @Pattern(regexp = "new|in_progress|resolved|closed", message = "Status tidak dikenal") String status) {
}
