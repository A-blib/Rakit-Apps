package com.aris.templateapp.ui.common;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Geseran mendatar untuk isi yang bisa digulir ke samping di dalam ViewPager2 (tab yang juga bisa digeser).
 * <ul>
 *   <li>Selama isinya masih bisa digulir ke arah geseran, isi yang bergerak (ViewPager2 dilarang mengambil alih).</li>
 *   <li>Begitu mentok di ujung, atau geseran lebih tegak daripada mendatar, geseran dilepas ke induk
 *       sehingga ViewPager2 pindah tab (atau halaman digulir ke bawah).</li>
 * </ul>
 * Tanpa ini, ViewPager2 merebut setiap geseran mendatar sehingga daftar ke samping susah sekali digulir.
 */
public final class PagerAwareSwipe {

    private final int touchSlop;
    private float downX;
    private float downY;

    public PagerAwareSwipe(Context context) {
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    /** Untuk daftar ke samping (RecyclerView horizontal). */
    public static void attach(RecyclerView list) {
        PagerAwareSwipe swipe = new PagerAwareSwipe(list.getContext());
        list.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent event) {
                swipe.onTouch(rv, event);
                return false;
            }
        });
    }

    /** Dipanggil untuk setiap sentuhan di {@code view}, sebelum view itu sendiri memprosesnya. */
    public void onTouch(View view, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                if (view.canScrollHorizontally(1) || view.canScrollHorizontally(-1)) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - downX;
                float dy = event.getY() - downY;
                if (Math.abs(dx) > touchSlop || Math.abs(dy) > touchSlop) {
                    boolean horizontal = Math.abs(dx) > Math.abs(dy);
                    // Geser ke kiri (dx < 0) berarti isi bergerak ke kanan: arah gulir = -tanda(dx).
                    if (!horizontal || !view.canScrollHorizontally((int) -Math.signum(dx))) {
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                }
                break;
            default:
                break;
        }
    }
}
