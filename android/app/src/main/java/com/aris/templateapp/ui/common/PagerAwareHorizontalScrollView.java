package com.aris.templateapp.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.HorizontalScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * HorizontalScrollView yang "tahu diri" saat berada di dalam ViewPager2 (mis. baris chip filter di tab yang juga
 * bisa digeser ke samping).
 * <ul>
 *   <li>Selama isinya masih bisa digulir ke arah geseran, isi yang bergerak (ViewPager2 dilarang mengambil alih).</li>
 *   <li>Begitu mentok di ujung, atau geseran lebih tegak daripada mendatar, geseran dilepas ke induk
 *       sehingga ViewPager2 pindah tab.</li>
 * </ul>
 * HorizontalScrollView biasa selalu mengunci geseran untuk dirinya sendiri, walau sudah mentok, sehingga
 * pengguna "terjebak" di tab itu saat menggeser di atas chip.
 */
public class PagerAwareHorizontalScrollView extends HorizontalScrollView {

    private final int touchSlop;
    private float downX;
    private float downY;

    public PagerAwareHorizontalScrollView(@NonNull Context context) {
        this(context, null);
    }

    public PagerAwareHorizontalScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                if (canScrollHorizontally(1) || canScrollHorizontally(-1)) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - downX;
                float dy = event.getY() - downY;
                if (Math.abs(dx) > touchSlop || Math.abs(dy) > touchSlop) {
                    boolean horizontal = Math.abs(dx) > Math.abs(dy);
                    // Geser ke kiri (dx < 0) berarti isi bergerak ke kanan: arah gulir = -tanda(dx).
                    if (!horizontal || !canScrollHorizontally((int) -Math.signum(dx))) {
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(event);
    }
}
