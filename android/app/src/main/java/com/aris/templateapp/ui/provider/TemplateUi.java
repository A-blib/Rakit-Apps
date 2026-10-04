package com.aris.templateapp.ui.provider;

import android.content.Context;

import androidx.annotation.StringRes;

import com.aris.templateapp.R;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Teks tampilan untuk nilai template dari backend: status, kategori, waktu relatif, angka. */
public final class TemplateUi {

    public static final String PUBLISHED = "published";
    public static final String CHECKING = "checking";
    public static final String CHECK_FAILED = "check_failed";
    public static final String DRAFT = "draft";
    public static final String DISABLED = "disabled";

    private TemplateUi() {
    }

    @StringRes
    public static int statusLabel(String status) {
        if (status == null) {
            return R.string.template_status_draft;
        }
        switch (status) {
            case PUBLISHED:
                return R.string.template_status_published;
            case CHECKING:
                return R.string.template_status_checking;
            case CHECK_FAILED:
                return R.string.template_status_check_failed;
            case DISABLED:
                return R.string.template_status_disabled;
            case DRAFT:
            default:
                return R.string.template_status_draft;
        }
    }

    /** Sama dengan pilihan "Tujuan website" di onboarding (enum WebsitePurpose di backend). */
    @StringRes
    public static int categoryLabel(String category) {
        if (category == null) {
            return R.string.purpose_lainnya;
        }
        switch (category) {
            case "sekolah":
                return R.string.purpose_sekolah;
            case "organisasi":
                return R.string.purpose_organisasi;
            case "umkm":
                return R.string.purpose_umkm;
            case "instansi":
                return R.string.purpose_instansi;
            case "pribadi":
                return R.string.purpose_pribadi;
            default:
                return R.string.purpose_lainnya;
        }
    }

    /**
     * Badge status + jumlah masalah, mis. "Tayang · 3 peringatan" atau "Tidak lolos · 2 error"
     * (huruf kapital dipasang oleh style Label).
     */
    public static String statusLine(Context context, String status, int errorCount, int warningCount) {
        String label = context.getString(statusLabel(status));
        if (CHECK_FAILED.equals(status) && errorCount > 0) {
            return context.getString(R.string.template_status_with_count, label,
                    context.getString(R.string.template_errors, errorCount));
        }
        if (warningCount > 0 && !DRAFT.equals(status)) {
            return context.getString(R.string.template_status_with_count, label,
                    context.getString(R.string.template_warnings, warningCount));
        }
        return label;
    }

    /** 1234 → "1.234" (pemisah ribuan Indonesia). */
    public static String count(long value) {
        return NumberFormat.getIntegerInstance(DownloadTrendChart.INDONESIA).format(value);
    }

    /** "4 Okt 11.40" menurut zona waktu HP; string kosong jika waktu tidak bisa dibaca. */
    public static String dateTime(String isoTime) {
        try {
            return DateTimeFormatter.ofPattern("d MMM HH.mm", DownloadTrendChart.INDONESIA)
                    .format(Instant.parse(isoTime).atZone(ZoneId.systemDefault()));
        } catch (DateTimeParseException | NullPointerException e) {
            return "";
        }
    }

    /** "Diperbarui 2 hari lalu". Waktu yang tidak bisa dibaca menghasilkan string kosong. */
    public static String updatedAgo(Context context, String isoTime) {
        Instant then;
        try {
            then = Instant.parse(isoTime);
        } catch (DateTimeParseException | NullPointerException e) {
            return "";
        }
        RelativeTime relative = RelativeTime.between(then, Instant.now());
        long n = relative.getAmount();
        switch (relative.getUnit()) {
            case JUST_NOW:
                return context.getString(R.string.updated_just_now);
            case MINUTES:
                return context.getString(R.string.updated_minutes_ago, n);
            case HOURS:
                return context.getString(R.string.updated_hours_ago, n);
            case DAYS:
                return context.getString(R.string.updated_days_ago, n);
            case WEEKS:
                return context.getString(R.string.updated_weeks_ago, n);
            case MONTHS:
                return context.getString(R.string.updated_months_ago, n);
            case YEARS:
            default:
                return context.getString(R.string.updated_years_ago, n);
        }
    }
}
