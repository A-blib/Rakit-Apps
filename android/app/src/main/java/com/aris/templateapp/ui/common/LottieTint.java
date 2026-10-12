package com.aris.templateapp.ui.common;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieValueCallback;
import com.aris.templateapp.R;

/**
 * Mewarnai ulang animasi Lottie mengikuti tema (revisi-fondasi-orgspace.md 7.5). File JSON dari
 * tools/generate_lottie.py menamai setiap garis/isian "stroke-primary", "fill-accent", dst.; di sini elemen
 * bernama itu diberi color_primary atau color_accent, yang berbeda di mode terang & gelap.
 * "**" berarti "di layer/grup mana pun".
 */
public final class LottieTint {

    private LottieTint() {
    }

    public static void apply(LottieAnimationView view) {
        int primary = ContextCompat.getColor(view.getContext(), R.color.color_primary);
        int accent = ContextCompat.getColor(view.getContext(), R.color.color_accent);
        setStroke(view, "stroke-primary", primary);
        setStroke(view, "stroke-accent", accent);
        setFill(view, "fill-primary", primary);
        setFill(view, "fill-accent", accent);
    }

    private static void setStroke(LottieAnimationView view, String name, @ColorInt int color) {
        view.addValueCallback(new KeyPath("**", name), LottieProperty.STROKE_COLOR, new LottieValueCallback<>(color));
    }

    private static void setFill(LottieAnimationView view, String name, @ColorInt int color) {
        // Opasitas isian (mis. 60%) tetap mengikuti file JSON; di sini hanya warnanya yang diganti.
        view.addValueCallback(new KeyPath("**", name), LottieProperty.COLOR, new LottieValueCallback<>(color));
    }
}
