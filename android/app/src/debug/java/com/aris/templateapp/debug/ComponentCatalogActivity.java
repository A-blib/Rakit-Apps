package com.aris.templateapp.debug;

import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.ActivityComponentCatalogBinding;
import com.aris.templateapp.ui.common.StatusBannerView;

/**
 * Katalog komponen (khusus build debug): menampilkan semua token desain dan style komponen di satu layar,
 * untuk memeriksa tampilan di mode terang dan gelap tanpa harus membuka layar app satu per satu.
 */
public class ComponentCatalogActivity extends AppCompatActivity {

    private static final int[] COLOR_TOKENS = {
            R.color.color_background, R.color.color_surface, R.color.color_surface_subtle,
            R.color.color_foreground, R.color.color_muted, R.color.color_border, R.color.color_border_strong,
            R.color.color_link, R.color.color_success, R.color.color_warning, R.color.color_error};

    private static final int[] ICONS = {
            R.drawable.ic_arrow_forward, R.drawable.ic_person, R.drawable.ic_settings, R.drawable.ic_swap_horiz,
            R.drawable.ic_storefront, R.drawable.ic_web, R.drawable.ic_logout, R.drawable.ic_wifi_off};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        ActivityComponentCatalogBinding binding = ActivityComponentCatalogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        binding.toggleTheme.setOnClickListener(v -> toggleNightMode());
        binding.loadingState.showLoading();
        binding.inputWithError.setError(getString(R.string.catalog_input_error));
        binding.selectableCard.setOnClickListener(v -> {
            boolean checked = !binding.selectableCard.isChecked();
            binding.selectableCard.setChecked(checked);
            // Kartu terpilih: garis 2dp warna foreground (warnanya dari selector_card_stroke).
            binding.selectableCard.setStrokeWidth(getResources().getDimensionPixelSize(
                    checked ? R.dimen.border_width_selected : R.dimen.border_width));
        });

        binding.bannerInfo.bind(StatusBannerView.Kind.INFO, getString(R.string.catalog_banner_info));
        binding.bannerWarning.bind(StatusBannerView.Kind.WARNING, getString(R.string.catalog_banner_pending));
        binding.bannerSuccess.bind(StatusBannerView.Kind.SUCCESS, getString(R.string.catalog_banner_success));
        binding.bannerError.bind(StatusBannerView.Kind.ERROR, getString(R.string.catalog_banner_error));

        addColorSwatches(binding.colorList);
        addIcons(binding.iconList);
    }

    /** Satu baris per token warna: kotak contoh + nama token (diambil dari nama resource). */
    private void addColorSwatches(LinearLayout container) {
        int size = getResources().getDimensionPixelSize(R.dimen.space_8);
        int gap = getResources().getDimensionPixelSize(R.dimen.space_3);
        for (int color : COLOR_TOKENS) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(0, gap / 2, 0, gap / 2);

            // Kotak contoh diberi garis tepi agar warna yang sama dengan latar (mis. color_background) tetap terlihat.
            GradientDrawable fill = new GradientDrawable();
            fill.setColor(getColor(color));
            fill.setCornerRadius(getResources().getDimension(R.dimen.radius_small));
            fill.setStroke(getResources().getDimensionPixelSize(R.dimen.border_width), getColor(R.color.color_border_strong));
            View swatch = new View(this);
            swatch.setBackground(fill);
            row.addView(swatch, new LinearLayout.LayoutParams(size, size));

            TextView name = new TextView(this);
            name.setTextAppearance(R.style.TextAppearance_App_BodySmall);
            name.setText(getResources().getResourceEntryName(color));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginStart(gap);
            row.addView(name, params);
            container.addView(row);
        }
    }

    private void addIcons(LinearLayout container) {
        int size = getResources().getDimensionPixelSize(R.dimen.icon_size);
        int gap = getResources().getDimensionPixelSize(R.dimen.space_4);
        for (int icon : ICONS) {
            ImageView image = (ImageView) LayoutInflater.from(this).inflate(R.layout.item_catalog_icon, container, false);
            image.setImageResource(icon);
            image.setContentDescription(getResources().getResourceEntryName(icon));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMarginEnd(gap);
            container.addView(image, params);
        }
    }

    private void toggleNightMode() {
        boolean isNight = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        AppCompatDelegate.setDefaultNightMode(isNight
                ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);
    }
}
