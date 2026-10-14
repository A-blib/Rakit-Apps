package com.aris.templateapp.data.remote.dto;

/**
 * Body PUT /providers/me/templates/{id}/folder. {@code folderId} null = keluarkan dari folder. Gson tidak menulis
 * field null, dan backend membaca field yang tidak ada sebagai null, jadi hasilnya sama.
 */
public class MoveTemplateDto {
    public final String folderId;

    public MoveTemplateDto(String folderId) {
        this.folderId = folderId;
    }
}
