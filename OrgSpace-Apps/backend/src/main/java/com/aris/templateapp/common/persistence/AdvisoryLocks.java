package com.aris.templateapp.common.persistence;

import com.aris.templateapp.common.util.Emails;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kunci antrean PostgreSQL ({@code pg_advisory_xact_lock}) untuk pola "cek dulu, lalu simpan". Tanpa kunci, dua
 * request yang sama persis (tombol Daftar diketuk dua kali, request dikirim ulang) sama-sama lolos pengecekan, lalu
 * yang kedua ditolak index unik database dan menjadi error 500. Dengan kunci, request kedua menunggu sampai yang
 * pertama selesai, lalu pengecekannya melihat data yang baru dibuat dan menjawab dengan benar.
 * <p>
 * Kunci otomatis lepas saat transaksi selesai, jadi wajib dipanggil di dalam transaksi pemanggil.
 */
@Component
@RequiredArgsConstructor
public class AdvisoryLocks {

    private final JdbcClient jdbc;

    /** Mengantrekan semua pembuatan akun dengan email yang sama (daftar email, Google, GitHub). */
    @Transactional(propagation = Propagation.MANDATORY)
    public void lockEmail(String email) {
        lock("email:" + Emails.normalize(email));
    }

    /** Mengantrekan login pertama dengan identitas Google/GitHub yang sama. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void lockIdentity(String provider, String providerUserId) {
        lock("identity:" + provider + ":" + providerUserId);
    }

    private void lock(String key) {
        jdbc.sql("SELECT pg_advisory_xact_lock(hashtextextended(:key, 0))").param("key", key).query().listOfRows();
    }
}
