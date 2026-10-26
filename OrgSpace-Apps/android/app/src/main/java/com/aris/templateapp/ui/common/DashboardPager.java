package com.aris.templateapp.ui.common;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.OneShotPreDrawListener;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.aris.templateapp.R;

import java.util.concurrent.TimeUnit;

/**
 * Membuka dashboard bertab (pembuat website & provider) dengan transisi yang terlihat.
 * <p>
 * Membuat dashboard + tab pertama butuh ±0,8 detik di thread utama. Jika transisi masuk (380 ms) langsung
 * berjalan, seluruh animasinya habis selama layar macet dan dashboard terlihat muncul tiba-tiba. Karena itu
 * transisi ditahan sampai tab pertama siap digambar (layar sebelumnya tetap tampil), dan tab lain baru dibuat
 * setelah transisi selesai.
 */
public final class DashboardPager {

    /** Batas menahan transisi, jika tab pertama tidak kunjung siap. */
    private static final long MAX_POSTPONE_SECONDS = 1;

    private DashboardPager() {
    }

    /** Dipanggil di onViewCreated dashboard, setelah adapter dipasang ke pager. */
    public static void enter(Fragment dashboard, ViewPager2 pager, int tabCount) {
        dashboard.postponeEnterTransition(MAX_POSTPONE_SECONDS, TimeUnit.SECONDS);
        // Animasi baru mulai pada frame berikutnya (frame pertama tab ±80 ms), jadi tab lain ditunda dua kali
        // durasi transisi agar pembuatannya tidak memotong ujung animasi.
        long otherTabsDelayMs = 2L * dashboard.getResources().getInteger(R.integer.slide_up_duration_ms);
        dashboard.getChildFragmentManager().registerFragmentLifecycleCallbacks(
                new FragmentManager.FragmentLifecycleCallbacks() {
                    @Override
                    public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment tab,
                                                      @NonNull View view, @Nullable Bundle savedInstanceState) {
                        fm.unregisterFragmentLifecycleCallbacks(this);
                        OneShotPreDrawListener.add(view, () -> {
                            dashboard.startPostponedEnterTransition();
                            pager.postDelayed(() -> pager.setOffscreenPageLimit(tabCount - 1), otherTabsDelayMs);
                        });
                    }
                }, false);
    }
}
