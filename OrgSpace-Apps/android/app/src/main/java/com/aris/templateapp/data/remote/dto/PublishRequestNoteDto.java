package com.aris.templateapp.data.remote.dto;

/** Body "Minta diterbitkan": catatan opsional untuk Admin/Pemilik. */
public class PublishRequestNoteDto {
    public final String note;

    public PublishRequestNoteDto(String note) {
        this.note = note;
    }
}
