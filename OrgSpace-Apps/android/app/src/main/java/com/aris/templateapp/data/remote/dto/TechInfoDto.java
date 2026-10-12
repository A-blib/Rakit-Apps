package com.aris.templateapp.data.remote.dto;

import java.util.List;

/** Info teknis otomatis hasil pengecekan (alur-fitur-upload.md bagian 6.1). */
public class TechInfoDto {
    public List<String> pages;
    public List<String> libraries;
    public long totalBytes;
    public boolean responsive;
    public List<CssVariableDto> cssVariables;

    public static class CssVariableDto {
        public String name;
        public String value;
    }
}
