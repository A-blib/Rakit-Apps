package com.aris.templateapp.user.dto;

import com.aris.templateapp.user.ActiveMode;
import jakarta.validation.constraints.NotNull;

/** @param mode "creator" atau "provider" */
public record ActiveModeRequest(
        @NotNull(message = "Mode wajib diisi")
        ActiveMode mode) {
}
