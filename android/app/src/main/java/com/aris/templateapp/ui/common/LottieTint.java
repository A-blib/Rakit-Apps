package com.aris.templateapp.ui.common;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieValueCallback;
import com.aris.templateapp.R;

/**
 * Mewarnai ulang animasi Lottie mengikuti tema (bagian 9.3). File JSON digambar dengan garis hitam;
 * di sini semua garis ("**" = semua layer) diganti warna color_foreground, yang berbeda di mode terang & gelap.
 */
public final class LottieTint {

    private LottieTint() {
    }

    public static void applyForeground(LottieAnimationView view) {
        int color = ContextCompat.getColor(view.getContext(), R.color.color_foreground);
        view.addValueCallback(new KeyPath("**"), LottieProperty.COLOR_FILTER,
                new LottieValueCallback<>(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)));
    }
}
