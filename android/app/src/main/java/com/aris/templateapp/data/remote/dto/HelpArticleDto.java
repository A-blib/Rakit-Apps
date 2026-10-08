package com.aris.templateapp.data.remote.dto;

/** Artikel Panduan per aturan pengecekan (GET /help/articles/{code}). */
public class HelpArticleDto {
    public String code;
    public String title;
    public String why;
    public String wrongExample;
    public String rightExample;
    public String howToFix;
    public String tips;
}
