package com.aris.templateapp.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** @param deviceId id instalasi app pemegang kunci */
public record LockRequest(@NotBlank(message = "deviceId wajib diisi") @Size(max = 64) String deviceId) {
}
