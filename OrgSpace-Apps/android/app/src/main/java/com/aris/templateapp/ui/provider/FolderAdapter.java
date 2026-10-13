package com.aris.templateapp.ui.provider;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.aris.templateapp.R;
import com.aris.templateapp.data.remote.dto.TemplateFolderDto;
import com.aris.templateapp.databinding.ItemTemplateFolderBinding;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Baris folder di halaman utama "Template Anda". Ketuk = buka folder, ⋮ = ganti nama / hapus. */
public class FolderAdapter extends ListAdapter<TemplateFolderDto, FolderAdapter.Holder> {

    private final Consumer<TemplateFolderDto> onOpen;
    private final BiConsumer<TemplateFolderDto, View> onMenu;

    public FolderAdapter(Consumer<TemplateFolderDto> onOpen, BiConsumer<TemplateFolderDto, View> onMenu) {
        super(DIFF);
        this.onOpen = onOpen;
        this.onMenu = onMenu;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemTemplateFolderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        TemplateFolderDto folder = getItem(position);
        ItemTemplateFolderBinding b = holder.binding;
        Context context = b.getRoot().getContext();
        b.name.setText(folder.name);
        b.count.setText(countLabel(context, folder.templateCount));
        b.getRoot().setOnClickListener(v -> onOpen.accept(folder));
        b.getRoot().setOnLongClickListener(v -> {
            onMenu.accept(folder, b.menuButton);
            return true;
        });
        b.menuButton.setOnClickListener(v -> onMenu.accept(folder, v));
    }

    static String countLabel(Context context, long count) {
        return count == 0 ? context.getString(R.string.folder_empty_count)
                : context.getResources().getQuantityString(R.plurals.folder_template_count, (int) count, (int) count);
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemTemplateFolderBinding binding;

        Holder(ItemTemplateFolderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
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
