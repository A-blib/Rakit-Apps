package com.aris.templateapp.help;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** Tabel {@code help_articles}: satu artikel Panduan per kode aturan pengecekan (alur-fitur-upload.md bagian 5.11). */
@Entity
@Table(name = "help_articles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HelpArticle {

    @Id
    @Column(length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false)
    private String why;

    private String wrongExample;

    private String rightExample;

    @Column(nullable = false)
    private String howToFix;

    private String tips;

    @Column(nullable = false)
    private Instant updatedAt;
}
