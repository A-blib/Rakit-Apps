package com.aris.templateapp.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.ViewStateBinding;

/**
 * Tampilan status untuk layar yang memuat data (bagian 8): loading (animasi), kosong (boleh dengan satu tombol aksi), atau error + "Coba lagi".
 * Saat data berhasil dimuat, panggil {@link #hide()} lalu tampilkan isi layar.
 */
public class StateView extends FrameLayout {

    private final ViewStateBinding binding;

    public StateView(@NonNull Context context) {
        this(context, null);
    }

    public StateView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        binding = ViewStateBinding.inflate(LayoutInflater.from(context), this);
        setVisibility(GONE);
    }

    public void showLoading() {
        show(R.raw.loading, getContext().getString(R.string.state_loading), null, null);
    }

    public void showEmpty(CharSequence message) {
        show(R.raw.empty, message, null, null);
    }

    /** Kosong + satu tombol aksi, mis. "Hapus filter" atau "Buka panduan". */
    public void showEmpty(CharSequence message, CharSequence actionText, Runnable action) {
        show(R.raw.empty, message, actionText, action);
    }

    public void showError(CharSequence message, Runnable retry) {
        show(R.raw.empty, message, getContext().getString(R.string.action_retry), retry);
    }

    public void hide() {
        binding.animation.cancelAnimation();
        setVisibility(GONE);
    }

    private void show(int animation, CharSequence message, @Nullable CharSequence actionText,
                      @Nullable Runnable retry) {
        setVisibility(VISIBLE);
        binding.animation.setAnimation(animation);
        // Warna dipasang ulang setiap ganti animasi: pengaturan warna Lottie melekat pada animasi yang sedang
        // dimuat, sehingga hilang saat berganti (mis. loading → kosong) dan garis hitam tak terlihat di mode gelap.
        LottieTint.applyForeground(binding.animation);
        binding.animation.playAnimation();
        binding.message.setText(message);
        binding.retryButton.setText(actionText);
        binding.retryButton.setVisibility(retry == null ? View.GONE : View.VISIBLE);
        binding.retryButton.setOnClickListener(retry == null ? null : v -> retry.run());
    }
}
