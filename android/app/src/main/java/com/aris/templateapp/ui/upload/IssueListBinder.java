package com.aris.templateapp.ui.upload;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.aris.templateapp.R;
import com.aris.templateapp.data.remote.dto.TemplateDetailDto.IssueDto;
import com.aris.templateapp.databinding.ItemIssueBinding;

import java.util.List;

/**
 * Daftar masalah hasil pengecekan, dipakai layar "Belum memenuhi standar" dan Detail template. Setiap masalah punya
 * tautan ke artikel Panduan (bagian 5.11); setiap Error punya tautan "Ini keliru? Laporkan" (bagian 5.9).
 */
public final class IssueListBinder {

    /** Aksi pada satu masalah. */
    public interface Actions {
        void onLearn(IssueDto issue);

        void onReport(IssueDto issue);
    }

    private IssueListBinder() {
    }

    public static void bind(LinearLayout list, List<IssueDto> issues, boolean errors, @Nullable Actions actions) {
        list.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(list.getContext());
        @DrawableRes int icon = errors ? R.drawable.ic_error : R.drawable.ic_warning;
        @ColorRes int color = errors ? R.color.color_error : R.color.color_warning;
        for (IssueDto issue : issues) {
            ItemIssueBinding row = ItemIssueBinding.inflate(inflater, list, true);
            row.icon.setImageResource(icon);
            row.icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(list.getContext(), color)));
            row.title.setText(issue.title);
            row.title.setVisibility(issue.title == null ? View.GONE : View.VISIBLE);
            row.message.setText(issue.message);
            if (issue.title == null) {
                // Tanpa judul, pesan menjadi baris utama: tebal dan tanpa jarak atas.
                row.message.setTypeface(ResourcesCompat.getFont(list.getContext(), R.font.geist_medium));
                ((ViewGroup.MarginLayoutParams) row.message.getLayoutParams()).topMargin = 0;
            }
            String location = issue.file == null ? null : issue.line == null ? issue.file
                    : list.getContext().getString(R.string.issue_location_line, issue.file, issue.line);
            row.location.setText(location);
            row.location.setVisibility(location == null ? View.GONE : View.VISIBLE);
            row.suggestion.setText(issue.suggestion);
            row.suggestion.setVisibility(issue.suggestion == null ? View.GONE : View.VISIBLE);
            if (actions == null || issue.code == null) {
                continue;
            }
            row.learnButton.setVisibility(View.VISIBLE);
            row.learnButton.setOnClickListener(v -> actions.onLearn(issue));
            // "Ini keliru?" hanya untuk Error dan hanya jika masalah punya id (data dari pengecekan upload).
            if (errors && issue.id != null) {
                row.reportButton.setVisibility(View.VISIBLE);
                row.reportButton.setEnabled(!issue.reported);
                row.reportButton.setText(issue.reported ? R.string.issue_reported : R.string.issue_report);
                if (issue.reported) {
                    row.reportButton.setTextColor(ContextCompat.getColor(list.getContext(), R.color.color_muted));
                }
                row.reportButton.setOnClickListener(v -> actions.onReport(issue));
            }
        }
    }
}
