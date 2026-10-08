package com.aris.templateapp.upload.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** Langkah wizard yang sedang dibuka, agar "Lanjutkan draft" membuka tepat di langkah itu. */
public record DraftStepRequest(@Min(3) @Max(6) int step) {
}
