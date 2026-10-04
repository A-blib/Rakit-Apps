package com.aris.templateapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.core.storage.SessionStore;
import com.aris.templateapp.databinding.ActivityMainBinding;
import com.aris.templateapp.ui.auth.AuthDeepLinks;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Satu-satunya Activity. Semua layar adalah Fragment yang ditukar oleh Navigation Component
 * di dalam {@code nav_host_fragment} (lihat res/navigation/nav_graph.xml).
 */
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject
    SessionStore sessionStore;

    @Inject
    AuthDeepLinks authDeepLinks;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Wajib dipanggil sebelum super.onCreate: menyambung splash sistem dengan tema app. Splash tidak ditahan:
        // selama layar pertama diputuskan, StartupFragment menampilkan animasi loading pahlawan.
        SplashScreen.installSplashScreen(this);
        // Konten digambar sampai ke balik status bar & navigation bar (wajib mulai Android 15);
        // EdgeToEdge juga memilih warna ikon status bar yang kontras untuk mode terang/gelap.
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Beri jarak sebesar status bar, navigation bar, dan keyboard agar konten tidak tertutup.
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        // Deep link yang membuka app dari keadaan tertutup. Saat Activity dibuat ulang (mis. HP diputar),
        // savedInstanceState tidak null dan deep link yang sama tidak diproses lagi.
        if (savedInstanceState == null) {
            handleDeepLink(getIntent());
        }

        sessionStore.sessionExpiredEvents().observe(this, event -> {
            if (event.getContentIfNotHandled() != null) {
                // Sesi berakhir (refresh token ditolak): kembali ke Dashboard Pembuat Website sebagai tamu.
                NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);
                if (host != null) {
                    host.getNavController().navigate(R.id.creatorDashboardFragment, null,
                            new NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build());
                }
                Snackbar.make(binding.getRoot(), R.string.error_session_expired, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    /** Dipanggil (karena launchMode singleTask) saat deep link datang ketika app sudah terbuka. */
    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        Uri uri = intent == null ? null : intent.getData();
        if (AuthDeepLinks.isAuthCallback(uri)) {
            authDeepLinks.publish(uri);
        }
    }
}
