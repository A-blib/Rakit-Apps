package com.aris.templateapp.ui.creator;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.aris.templateapp.R;
import com.aris.templateapp.data.local.ProjectEntity;
import com.aris.templateapp.data.model.ProjectMode;
import com.aris.templateapp.data.model.ProjectStatus;
import com.aris.templateapp.ui.common.RelativeTime;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Teks & gaya kartu project (alur-pembuatan-website.md bagian 4.2–4.3). */
public final class ProjectUi {

    private static final DateTimeFormatter EXPORT_DATE =
            DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("id-ID"));

    private ProjectUi() {
    }

    @StringRes
    public static int statusLabel(ProjectStatus status) {
        switch (status) {
            case READY:
                return R.string.project_status_ready;
            case EXPORTED:
                return R.string.project_status_exported;
            case DRAFT:
            default:
                return R.string.project_status_draft;
        }
    }

    @StringRes
    public static int modeLabel(ProjectMode mode) {
        return mode == ProjectMode.TEMPLATE ? R.string.project_mode_template : R.string.project_mode_custom;
    }

    /** Badge status: DRAFT abu-abu, SIAP EXPORT hijau, DIEXPORT hanya garis. */
    public static void bindStatusBadge(TextView badge, ProjectStatus status) {
        Context context = badge.getContext();
        badge.setText(statusLabel(status));
        switch (status) {
            case READY:
                badge.setBackgroundResource(R.drawable.bg_badge_success);
                badge.setTextColor(ContextCompat.getColor(context, R.color.color_success));
                break;
            case EXPORTED:
                badge.setBackgroundResource(R.drawable.bg_badge_outline);
                badge.setTextColor(ContextCompat.getColor(context, R.color.color_foreground));
                break;
            case DRAFT:
            default:
                badge.setBackgroundResource(R.drawable.bg_badge);
                badge.setTextColor(ContextCompat.getColor(context, R.color.color_muted));
                break;
        }
    }

    /** "Diedit 2 jam lalu". */
    public static String editedAgo(Context context, long updatedAt) {
        RelativeTime relative = RelativeTime.between(Instant.ofEpochMilli(updatedAt), Instant.now());
        long n = relative.getAmount();
        switch (relative.getUnit()) {
            case JUST_NOW:
                return context.getString(R.string.project_edited_just_now);
            case MINUTES:
                return context.getString(R.string.project_edited_minutes_ago, n);
            case HOURS:
                return context.getString(R.string.project_edited_hours_ago, n);
            case DAYS:
                return context.getString(R.string.project_edited_days_ago, n);
            case WEEKS:
                return context.getString(R.string.project_edited_weeks_ago, n);
            case MONTHS:
                return context.getString(R.string.project_edited_months_ago, n);
            case YEARS:
            default:
                return context.getString(R.string.project_edited_years_ago, n);
        }
    }

    /** Baris waktu kartu: "Diedit 2 jam lalu", ditambah "· Diexport 3 Okt" untuk project yang sudah diexport. */
    public static String timeLine(Context context, ProjectEntity project) {
        String edited = editedAgo(context, project.updatedAt);
        if (project.status == ProjectStatus.EXPORTED && project.lastExportedAt != null) {
            String date = EXPORT_DATE.format(Instant.ofEpochMilli(project.lastExportedAt).atZone(ZoneId.systemDefault()));
            return context.getString(R.string.template_status_with_count, edited,
                    context.getString(R.string.project_exported_on, date));
        }
        return edited;
    }

    /**
     * Info tambahan (bagian 4.3): "3 isian belum diisi" untuk draft, "Ada perubahan sejak export terakhir" untuk
     * project yang diedit setelah diexport, atau null.
     */
    @Nullable
    public static String info(Context context, ProjectEntity project) {
        if (project.hasChangesSinceExport()) {
            return context.getString(R.string.project_changed_since_export);
        }
        if (project.status == ProjectStatus.DRAFT && project.missingCount > 0) {
            return context.getString(R.string.project_missing, project.missingCount);
        }
        return null;
    }

    /** Info tambahan berwarna peringatan jika project perlu diexport ulang. */
    public static void bindInfo(TextView view, ProjectEntity project) {
        String info = info(view.getContext(), project);
        view.setText(info);
        view.setVisibility(info == null ? android.view.View.GONE : android.view.View.VISIBLE);
        view.setTextColor(ContextCompat.getColor(view.getContext(),
                project.hasChangesSinceExport() ? R.color.color_warning : R.color.color_muted));
    }

    /** Baris ringkas kartu Beranda: "DRAFT · DIEDIT 2 JAM LALU" (huruf kapital dari style Label). */
    public static String compactStatus(Context context, ProjectEntity project) {
        return context.getString(R.string.template_status_with_count, context.getString(statusLabel(project.status)),
                editedAgo(context, project.updatedAt));
    }
}
