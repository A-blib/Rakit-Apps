package com.aris.templateapp.ui.website;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.aris.templateapp.R;
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.data.model.Website;
import com.aris.templateapp.databinding.ItemWebsiteBinding;
import com.aris.templateapp.ui.creator.ProjectUi;

/** Kartu website di tab Website. Ketuk kartu → halaman kelola; ⋮ → menu kartu. */
public class WebsiteAdapter extends ListAdapter<Website, WebsiteAdapter.Holder> {

    public interface Listener {
        void onOpen(Website website);

        void onMenu(View anchor, Website website);
    }

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
    private final Listener listener;

    public WebsiteAdapter(ThumbnailLoader thumbnailLoader, Listener listener) {
        super(DIFF);
        this.thumbnailLoader = thumbnailLoader;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemWebsiteBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Website website = getItem(position);
        ItemWebsiteBinding b = holder.binding;
        b.name.setText(website.name);
        WebsiteUi.bindStatusBadge(b.statusBadge, website.status);
        b.mode.setText(ProjectUi.modeLabel(website.mode));

        String address = WebsiteUi.shortAddress(website.siteUrl);
        b.address.setText(address);
        b.address.setVisibility(address == null ? View.GONE : View.VISIBLE);
        b.changed.setVisibility(website.isOnlineWithChanges() ? View.VISIBLE : View.GONE);
        b.updated.setText(WebsiteUi.updatedLine(b.getRoot().getContext(), website));

        boolean locked = website.lockedByName != null;
        b.lock.setVisibility(locked ? View.VISIBLE : View.GONE);
        if (locked) {
            b.lock.setText(b.getRoot().getContext().getString(R.string.website_locked_by, website.lockedByName));
        }
        b.missing.setVisibility(website.missingCount > 0 ? View.VISIBLE : View.GONE);
        if (website.missingCount > 0) {
            b.missing.setText(b.getRoot().getContext().getString(R.string.website_missing, website.missingCount));
        }

        b.thumbnail.setImageDrawable(null);
        thumbnailLoader.load(website.thumbnailUrl, b.thumbnail, null);
        b.menuButton.setContentDescription(b.getRoot().getContext().getString(R.string.cd_website_menu, website.name));
        b.getRoot().setOnClickListener(v -> listener.onOpen(website));
        b.menuButton.setOnClickListener(v -> listener.onMenu(v, website));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemWebsiteBinding binding;

        Holder(ItemWebsiteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
