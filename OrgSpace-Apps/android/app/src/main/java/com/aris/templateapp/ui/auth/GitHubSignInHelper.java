package com.aris.templateapp.ui.auth;

import android.content.Context;
import android.net.Uri;

import androidx.browser.customtabs.CustomTabsIntent;

/**
 * Membuka halaman login GitHub di Custom Tab (browser di dalam app, bagian 6.4). Setelah user login,
 * GitHub memanggil backend, lalu backend mengarahkan browser ke deep link templateapp://auth/callback
 * yang ditangkap MainActivity.
 */
public final class GitHubSignInHelper {

    private GitHubSignInHelper() {
    }

    public static void open(Context context, String url) {
        new CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
                .launchUrl(context, Uri.parse(url));
    }
}
