package com.aris.templateapp.ui.provider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.aris.templateapp.R;
import com.aris.templateapp.data.remote.dto.TemplateFolderDto;
import com.aris.templateapp.databinding.ItemTemplateFolderBinding;
import com.aris.templateapp.databinding.ItemTemplateFolderGridBinding;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Kartu folder di halaman utama "Template Anda".
 * Mendukung tampilan baris (1 kolom) dan kisi (2 kolom).
 */
public class FolderAdapter extends ListAdapter<TemplateFolderDto, FolderAdapter.Holder> {

    public static final int VIEW_TYPE_LINE = 0;
    public static final int VIEW_TYPE_GRID = 1;

    private final Consumer<TemplateFolderDto> onOpen;
    private final BiConsumer<TemplateFolderDto, View> onMenu;
    private boolean isGridMode = false;

    public FolderAdapter(Consumer<TemplateFolderDto> onOpen, BiConsumer<TemplateFolderDto, View> onMenu) {
        super(DIFF);
        this.onOpen = onOpen;
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
            ItemTemplateFolderGridBinding b = ItemTemplateFolderGridBinding.inflate(inflater, parent, false);
            return new Holder(b.getRoot(), b.name, b.count, b.menuButton);
        } else {
            ItemTemplateFolderBinding b = ItemTemplateFolderBinding.inflate(inflater, parent, false);
            return new Holder(b.getRoot(), b.name, b.count, b.menuButton);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        TemplateFolderDto folder = getItem(position);
        Context context = holder.itemView.getContext();
        holder.name.setText(folder.name);
        holder.count.setText(countLabel(context, folder.templateCount));
        holder.itemView.setOnClickListener(v -> onOpen.accept(folder));
        holder.itemView.setOnLongClickListener(v -> {
            onMenu.accept(folder, holder.menuButton);
            return true;
        });
        holder.menuButton.setOnClickListener(v -> onMenu.accept(folder, v));
    }

    static String countLabel(Context context, long count) {
        return count == 0 ? context.getString(R.string.folder_empty_count)
                : context.getResources().getQuantityString(R.plurals.folder_template_count, (int) count, (int) count);
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView count;
        final View menuButton;

        Holder(View root, TextView name, TextView count, View menuButton) {
            super(root);
            this.name = name;
            this.count = count;
            this.menuButton = menuButton;
        }
    }

    private static final DiffUtil.ItemCallback<TemplateFolderDto> DIFF = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull TemplateFolderDto a, @NonNull TemplateFolderDto b) {
            return Objects.equals(a.id, b.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull TemplateFolderDto a, @NonNull TemplateFolderDto b) {
            return Objects.equals(a.name, b.name) && a.templateCount == b.templateCount;
        }
    };
}
