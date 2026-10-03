package com.aris.templateapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.aris.templateapp.core.storage.SessionStore;
import com.aris.templateapp.databinding.ActivityMainBinding;
import com.aris.templateapp.ui.startup.StartupViewModel;
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

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Wajib dipanggil sebelum super.onCreate: menyambung splash sistem dengan tema app.
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        // Konten digambar sampai ke balik status bar & navigation bar (wajib mulai Android 15);
        // EdgeToEdge juga memilih warna ikon status bar yang kontras untuk mode terang/gelap.
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        // ViewModel baru boleh diminta SETELAH super.onCreate (Hilt butuh Activity yang sudah siap).
        // Splash ditahan sampai layar pertama diputuskan (mis. menunggu /users/me), agar tidak berkedip.
        StartupViewModel startupViewModel = new ViewModelProvider(this).get(StartupViewModel.class);
        splashScreen.setKeepOnScreenCondition(() -> startupViewModel.getDecision().getValue() == null);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Beri jarak sebesar status bar, navigation bar, dan keyboard agar konten tidak tertutup.
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        sessionStore.sessionExpiredEvents().observe(this, event -> {
            if (event.getContentIfNotHandled() != null) {
                Snackbar.make(binding.getRoot(), R.string.error_session_expired, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
