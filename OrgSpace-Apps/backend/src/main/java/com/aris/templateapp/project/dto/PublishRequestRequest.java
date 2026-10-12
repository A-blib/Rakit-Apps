package com.aris.templateapp.project.dto;

import jakarta.validation.constraints.Size;

/** "Minta diterbitkan" dari Editor, boleh disertai catatan singkat untuk Admin/Pemilik. */
public record PublishRequestRequest(
        @Size(max = 200, message = "Catatan maksimal 200 karakter")
        String note) {
}
