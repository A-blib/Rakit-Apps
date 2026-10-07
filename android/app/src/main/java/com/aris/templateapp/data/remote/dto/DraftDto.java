package com.aris.templateapp.data.remote.dto;

import java.util.List;

/** Isi draft untuk langkah 3 dan seterusnya (GET /providers/me/uploads/{id}). */
public class DraftDto {
    public String templateId;
    public String status;
    public int wizardStep;
    public String sourceFileName;
    public Long sourceSize;
    public String name;
    public String category;
    public String description;
    public List<String> keywords;
    public String thumbnailUrl;
    /** auto · section · custom */
    public String thumbnailSource;
    /** mobile · desktop */
    public String thumbnailView;
    public TechInfoDto techInfo;
    public int warningCount;
    /** Jumlah isian yang sudah disimpan (langkah 4). */
    public int fieldCount;
    /** Nama sama dengan template lain milik provider ini (Peringatan). */
    public boolean nameDuplicate;
    public String updatedAt;
}
