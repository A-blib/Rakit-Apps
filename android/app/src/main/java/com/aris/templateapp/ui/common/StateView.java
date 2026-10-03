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
 * Tampilan status untuk layar yang memuat data (bagian 8): loading (animasi), kosong, atau error + "Coba lagi".
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
        LottieTint.applyForeground(binding.animation);
        setVisibility(GONE);
    }

    public void showLoading() {
        show(R.raw.loading, getContext().getString(R.string.state_loading), null);
    }

    public void showEmpty(CharSequence message) {
        show(R.raw.empty, message, null);
    }

    public void showError(CharSequence message, Runnable retry) {
        show(R.raw.empty, message, retry);
    }

    public void hide() {
        binding.animation.cancelAnimation();
        setVisibility(GONE);
    }

    private void show(int animation, CharSequence message, @Nullable Runnable retry) {
        setVisibility(VISIBLE);
        binding.animation.setAnimation(animation);
        binding.animation.playAnimation();
        binding.message.setText(message);
        binding.retryButton.setVisibility(retry == null ? View.GONE : View.VISIBLE);
        binding.retryButton.setOnClickListener(retry == null ? null : v -> retry.run());
    }
}
