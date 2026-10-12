package com.aris.templateapp.ui.website;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.data.model.Website;
import com.aris.templateapp.databinding.ItemProjectSmallBinding;

import java.util.function.Consumer;

/** Kartu kecil "Lanjutkan" di Beranda yang bisa digeser ke samping (alur-pembuatan-website.md 3.1). */
public class WebsiteSmallAdapter extends ListAdapter<Website, WebsiteSmallAdapter.Holder> {

    private static final DiffUtil.ItemCallback<Website> DIFF = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull Website a, @NonNull Website b) {
            return a.id.equals(b.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Website a, @NonNull Website b) {
            return a.equals(b);
        }
    };

    private final ThumbnailLoader thumbnailLoader;
    private final Consumer<Website> onClick;

    public WebsiteSmallAdapter(ThumbnailLoader thumbnailLoader, Consumer<Website> onClick) {
        super(DIFF);
        this.thumbnailLoader = thumbnailLoader;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemProjectSmallBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Website website = getItem(position);
        holder.binding.name.setText(website.name);
        holder.binding.status.setText(WebsiteUi.compactStatus(holder.itemView.getContext(), website));
        holder.binding.thumbnail.setImageDrawable(null);
        thumbnailLoader.load(website.thumbnailUrl, holder.binding.thumbnail, null);
        holder.itemView.setOnClickListener(v -> onClick.accept(website));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemProjectSmallBinding binding;

        Holder(ItemProjectSmallBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
