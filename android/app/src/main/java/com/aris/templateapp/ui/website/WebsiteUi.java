package com.aris.templateapp.ui.website;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.aris.templateapp.R;
import com.aris.templateapp.data.model.Website;
import com.aris.templateapp.data.model.WebsiteStatus;
import com.aris.templateapp.ui.common.RelativeTime;

import java.time.Instant;

/** Teks & badge website yang dipakai kartu tab Website dan halaman kelola (alur-pembuatan-website.md 4.2–4.3). */
public final class WebsiteUi {

    private WebsiteUi() {
    }

    @StringRes
    public static int statusLabel(WebsiteStatus status) {
        switch (status) {
            case ONLINE:
                return R.string.website_status_online;
            case OFFLINE:
                return R.string.website_status_offline;
            case SUSPENDED:
                return R.string.website_status_suspended;
            case DRAFT:
            default:
                return R.string.website_status_draft;
        }
    }

    /** Draft biru muda, Online hijau, Dimatikan hanya garis, Ditangguhkan merah. */
    public static void bindStatusBadge(TextView badge, WebsiteStatus status) {
        Context context = badge.getContext();
        badge.setText(statusLabel(status));
        switch (status) {
            case ONLINE:
                badge.setBackgroundResource(R.drawable.bg_badge_success);
                badge.setTextColor(ContextCompat.getColor(context, R.color.color_success));
                break;
            case OFFLINE:
                badge.setBackgroundResource(R.drawable.bg_badge_outline);
                badge.setTextColor(ContextCompat.getColor(context, R.color.color_foreground));
                break;
            case SUSPENDED:
                badge.setBackgroundResource(R.drawable.bg_badge_error);
                badge.setTextColor(ContextCompat.getColor(context, R.color.color_error));
                break;
            case DRAFT:
            default:
                badge.setBackgroundResource(R.drawable.bg_badge);
                badge.setTextColor(ContextCompat.getColor(context, R.color.color_on_primary_container));
                break;
        }
    }

    /** "Diubah 2 jam lalu oleh Budi" (tanpa "oleh" jika nama pengubah tidak diketahui). */
    public static String updatedLine(Context context, Website website) {
        String when = updatedAgo(context, website.updatedAt);
        return website.updatedByName == null ? when
                : context.getString(R.string.website_updated_by, when, website.updatedByName);
    }

    private static String updatedAgo(Context context, long millis) {
        RelativeTime relative = RelativeTime.between(Instant.ofEpochMilli(millis), Instant.now());
        long n = relative.getAmount();
        switch (relative.getUnit()) {
            case JUST_NOW:
                return context.getString(R.string.website_updated_just_now);
            case MINUTES:
                return context.getString(R.string.website_updated_minutes, n);
            case HOURS:
                return context.getString(R.string.website_updated_hours, n);
            case DAYS:
                return context.getString(R.string.website_updated_days, n);
            case WEEKS:
                return context.getString(R.string.website_updated_weeks, n);
            case MONTHS:
                return context.getString(R.string.website_updated_months, n);
            case YEARS:
            default:
                return context.getString(R.string.website_updated_years, n);
        }
    }

    /** "2 jam lalu" tanpa kata kerja (banner offline, permintaan terbit). */
    public static String ago(Context context, long millis) {
        RelativeTime relative = RelativeTime.between(Instant.ofEpochMilli(millis), Instant.now());
        long n = relative.getAmount();
        switch (relative.getUnit()) {
            case JUST_NOW:
                return context.getString(R.string.ago_just_now);
            case MINUTES:
                return context.getString(R.string.ago_minutes, n);
            case HOURS:
                return context.getString(R.string.ago_hours, n);
            case DAYS:
                return context.getString(R.string.ago_days, n);
            case WEEKS:
                return context.getString(R.string.ago_weeks, n);
            case MONTHS:
                return context.getString(R.string.ago_months, n);
            case YEARS:
            default:
                return context.getString(R.string.ago_years, n);
        }
    }

    /** Alamat tanpa "https://" agar muat di kartu, mis. "osis-smansa.orgspace.site". */
    @Nullable
    public static String shortAddress(@Nullable String url) {
        if (url == null) {
            return null;
        }
        int scheme = url.indexOf("://");
        return scheme < 0 ? url : url.substring(scheme + 3);
    }
}
