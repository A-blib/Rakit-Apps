package com.aris.templateapp.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * HorizontalScrollView yang "tahu diri" saat berada di dalam ViewPager2 (mis. baris chip filter di tab yang juga
 * bisa digeser ke samping). Aturannya ada di {@link PagerAwareSwipe}.
 * <p>
 * HorizontalScrollView biasa selalu mengunci geseran untuk dirinya sendiri, walau sudah mentok, sehingga
 * pengguna "terjebak" di tab itu saat menggeser di atas chip.
 */
public class PagerAwareHorizontalScrollView extends HorizontalScrollView {

    private final PagerAwareSwipe swipe;

    public PagerAwareHorizontalScrollView(@NonNull Context context) {
        this(context, null);
    }

    public PagerAwareHorizontalScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        swipe = new PagerAwareSwipe(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        swipe.onTouch(this, event);
        return super.dispatchTouchEvent(event);
    }
}
