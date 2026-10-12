package com.aris.templateapp.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FeedbackMessageRequest(
        @NotBlank(message = "Pesan wajib diisi") @Size(max = 4000, message = "Pesan maksimal 4000 karakter") String body) {
}
