package com.aris.templateapp.ui.common;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.ViewStatusBannerBinding;

/**
 * Banner status (bagian 9.3): kotak bergaris radius 8dp, ikon + teks berwarna semantik, latar tint tipis.
 * Dipakai untuk status provider (menunggu, ditolak, ditangguhkan) dan pesan informasi lain.
 */
public class StatusBannerView extends LinearLayout {

    /** Jenis banner menentukan warna dan ikon. */
    public enum Kind {
        INFO(R.drawable.bg_banner_link, R.color.color_link, R.drawable.ic_info),
        SUCCESS(R.drawable.bg_banner_success, R.color.color_success, R.drawable.ic_check_circle),
        WARNING(R.drawable.bg_banner_warning, R.color.color_warning, R.drawable.ic_schedule),
        ERROR(R.drawable.bg_banner_error, R.color.color_error, R.drawable.ic_error);

        @DrawableRes final int background;
        final int color;
        @DrawableRes final int icon;

        Kind(int background, int color, int icon) {
            this.background = background;
            this.color = color;
            this.icon = icon;
        }
    }

    private final ViewStatusBannerBinding binding;

    public StatusBannerView(@NonNull Context context) {
        this(context, null);
    }

    public StatusBannerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.space_3);
        setPadding(padding, padding, padding, padding);
        binding = ViewStatusBannerBinding.inflate(LayoutInflater.from(context), this);
        bind(Kind.INFO, null);
    }

    /** Ikon opsional: jika null, ikon bawaan jenis banner dipakai. */
    public void bind(Kind kind, CharSequence message, @Nullable @DrawableRes Integer icon) {
        setBackgroundResource(kind.background);
        ColorStateList color = ColorStateList.valueOf(ContextCompat.getColor(getContext(), kind.color));
        binding.bannerIcon.setImageResource(icon != null ? icon : kind.icon);
        binding.bannerIcon.setImageTintList(color);
        binding.bannerText.setTextColor(color);
        binding.bannerText.setText(message);
    }

    public void bind(Kind kind, CharSequence message) {
        bind(kind, message, null);
    }
}
