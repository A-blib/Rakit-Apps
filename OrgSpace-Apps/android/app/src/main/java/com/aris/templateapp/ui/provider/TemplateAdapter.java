package com.aris.templateapp.ui.provider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.aris.templateapp.R;
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.data.remote.dto.TemplateListDto.TemplateSummaryDto;
import com.aris.templateapp.databinding.ItemTemplateBinding;
import com.aris.templateapp.databinding.ItemTemplateGridBinding;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Kartu "Template Anda". Mendukung tampilan baris (1 kolom) dan kisi (2 kolom).
 * {@link ListAdapter} membandingkan daftar lama & baru (DiffUtil).
 */
public class TemplateAdapter extends ListAdapter<TemplateSummaryDto, TemplateAdapter.Holder> {

    public static final int VIEW_TYPE_LINE = 0;
    public static final int VIEW_TYPE_GRID = 1;

    private final ThumbnailLoader thumbnails;
    private final Consumer<TemplateSummaryDto> onClick;
    /** Menu ⋮ kartu (atau tekan lama): menerima template dan view jangkar menu. */
    private final BiConsumer<TemplateSummaryDto, View> onMenu;
    private boolean isGridMode = false;

    public TemplateAdapter(ThumbnailLoader thumbnails, Consumer<TemplateSummaryDto> onClick,
                           BiConsumer<TemplateSummaryDto, View> onMenu) {
        super(DIFF);
        this.thumbnails = thumbnails;
        this.onClick = onClick;
        this.onMenu = onMenu;
    }

    // Mengganti mode mengubah jenis view SEMUA item (kartu baris ↔ kisi), jadi seluruh daftar memang perlu digambar ulang.
    @SuppressLint("NotifyDataSetChanged")
    public void setGridMode(boolean isGrid) {
        if (this.isGridMode != isGrid) {
            this.isGridMode = isGrid;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return isGridMode ? VIEW_TYPE_GRID : VIEW_TYPE_LINE;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_GRID) {
            ItemTemplateGridBinding b = ItemTemplateGridBinding.inflate(inflater, parent, false);
            return new Holder(b.getRoot(), b.thumbnailPlaceholder, b.thumbnail, b.name, b.status, b.meta, b.menuButton);
        } else {
            ItemTemplateBinding b = ItemTemplateBinding.inflate(inflater, parent, false);
            return new Holder(b.getRoot(), b.thumbnailPlaceholder, b.thumbnail, b.name, b.status, b.meta, b.menuButton);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        TemplateSummaryDto item = getItem(position);
        Context context = holder.itemView.getContext();
        holder.thumbnailPlaceholder.setVisibility(View.VISIBLE);
        thumbnails.load(item.thumbnailUrl, holder.thumbnail, () -> holder.thumbnailPlaceholder.setVisibility(View.GONE));
        holder.name.setText(item.name);
        holder.status.setText(TemplateUi.statusLine(context, item.status, item.errorCount, item.warningCount));
        holder.status.setTextColor(ContextCompat.getColor(context, statusColor(item)));

        String category = TemplateUi.categoryLabel(item.category);
        String updated = TemplateUi.updatedAgo(context, item.updatedAt);
        // Angka dilihat/download hanya bermakna untuk template yang pernah tayang.
        boolean hasStats = TemplateUi.PUBLISHED.equals(item.status) || TemplateUi.DISABLED.equals(item.status);
        holder.meta.setText(hasStats
                ? context.getString(R.string.template_card_meta, category, item.views, item.downloads, updated)
                : context.getString(R.string.template_card_meta_short, category, updated));
        holder.itemView.setOnClickListener(v -> onClick.accept(item));
        holder.itemView.setOnLongClickListener(v -> {
            onMenu.accept(item, holder.menuButton);
            return true;
        });
        holder.menuButton.setOnClickListener(v -> onMenu.accept(item, v));
    }

    private static int statusColor(TemplateSummaryDto item) {
        if (TemplateUi.CHECK_FAILED.equals(item.status)) {
            return R.color.color_error;
        }
        if (TemplateUi.PUBLISHED.equals(item.status) && item.warningCount > 0) {
            return R.color.color_warning;
        }
        return R.color.color_muted;
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final View thumbnailPlaceholder;
        final ImageView thumbnail;
        final TextView name;
        final TextView status;
        final TextView meta;
        final View menuButton;

        Holder(View root, View thumbnailPlaceholder, ImageView thumbnail,
               TextView name, TextView status, TextView meta, View menuButton) {
            super(root);
            this.thumbnailPlaceholder = thumbnailPlaceholder;
            this.thumbnail = thumbnail;
            this.name = name;
            this.status = status;
            this.meta = meta;
            this.menuButton = menuButton;
        }
    }

    private static final DiffUtil.ItemCallback<TemplateSummaryDto> DIFF = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull TemplateSummaryDto a, @NonNull TemplateSummaryDto b) {
            return Objects.equals(a.id, b.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull TemplateSummaryDto a, @NonNull TemplateSummaryDto b) {
            return Objects.equals(a.name, b.name) && Objects.equals(a.status, b.status)
                    && Objects.equals(a.category, b.category) && Objects.equals(a.updatedAt, b.updatedAt)
                    && a.errorCount == b.errorCount && a.warningCount == b.warningCount
                    && a.views == b.views && a.downloads == b.downloads && Objects.equals(a.folderId, b.folderId);
        }
    };
}
