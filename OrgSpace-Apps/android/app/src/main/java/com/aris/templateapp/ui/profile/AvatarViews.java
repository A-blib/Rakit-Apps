package com.aris.templateapp.ui.profile;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.ui.common.AppBarAccount;

/**
 * Avatar profil = huruf depan nama di lingkaran, dengan foto di atasnya bila user punya foto. Saat tidak ada foto
 * (atau foto gagal dimuat), ImageView kosong sehingga huruf depan di bawahnya tetap terlihat.
 */
public final class AvatarViews {

    private AvatarViews() {
    }

    public static void bind(TextView initial, ImageView photo, ThumbnailLoader loader, @Nullable String name,
                            @Nullable String avatarUrl) {
        initial.setText(AppBarAccount.initialOf(name));
        loader.load(avatarUrl, photo, null);
    }
}
