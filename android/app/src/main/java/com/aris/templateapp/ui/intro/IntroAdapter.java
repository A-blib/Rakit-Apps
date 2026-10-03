package com.aris.templateapp.ui.intro;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.ItemIntroPageBinding;
import com.aris.templateapp.ui.common.LottieTint;

/** Isi 3 halaman intro untuk ViewPager2 (ViewPager2 memakai Adapter yang sama seperti RecyclerView). */
public class IntroAdapter extends RecyclerView.Adapter<IntroAdapter.PageHolder> {

    /** Data satu halaman: judul, penjelasan, dan animasi. */
    record Page(@StringRes int title, @StringRes int body, @RawRes int animation) {
    }

    static final Page[] PAGES = {
            new Page(R.string.intro_title_1, R.string.intro_body_1, R.raw.intro_build),
            new Page(R.string.intro_title_2, R.string.intro_body_2, R.raw.intro_customize),
            new Page(R.string.intro_title_3, R.string.intro_body_3, R.raw.intro_share),
    };

    @NonNull
    @Override
    public PageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemIntroPageBinding binding = ItemIntroPageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        LottieTint.applyForeground(binding.illustration);
        return new PageHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PageHolder holder, int position) {
        Page page = PAGES[position];
        holder.binding.title.setText(page.title());
        holder.binding.body.setText(page.body());
        holder.binding.illustration.setAnimation(page.animation());
        holder.binding.illustration.playAnimation();
    }

    @Override
    public int getItemCount() {
        return PAGES.length;
    }

    static class PageHolder extends RecyclerView.ViewHolder {
        final ItemIntroPageBinding binding;

        PageHolder(ItemIntroPageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
