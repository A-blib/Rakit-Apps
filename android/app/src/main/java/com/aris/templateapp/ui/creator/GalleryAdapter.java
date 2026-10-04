package com.aris.templateapp.ui.creator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.aris.templateapp.R;
import com.aris.templateapp.data.remote.dto.GalleryPageDto.GalleryTemplateDto;
import com.aris.templateapp.databinding.ItemGalleryCardBinding;
import com.aris.templateapp.databinding.ItemGalleryTemplateBinding;
import com.aris.templateapp.ui.provider.TemplateUi;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Kartu template galeri. Dua bentuk: {@link #list} untuk tab Template (list satu kolom, keputusan Aris) dan
 * {@link #cards} untuk "Template untuk anda" di Beranda (kartu kecil yang digeser ke samping).
 */
public class GalleryAdapter extends ListAdapter<GalleryTemplateDto, GalleryAdapter.Holder> {

    private final boolean compact;
    private final Consumer<GalleryTemplateDto> onClick;

    private GalleryAdapter(boolean compact, Consumer<GalleryTemplateDto> onClick) {
        super(DIFF);
        this.compact = compact;
        this.onClick = onClick;
    }

    public static GalleryAdapter list(Consumer<GalleryTemplateDto> onClick) {
        return new GalleryAdapter(false, onClick);
    }

    public static GalleryAdapter cards(Consumer<GalleryTemplateDto> onClick) {
        return new GalleryAdapter(true, onClick);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new Holder(compact ? ItemGalleryCardBinding.inflate(inflater, parent, false)
                : ItemGalleryTemplateBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        GalleryTemplateDto item = getItem(position);
        Context context = holder.binding.getRoot().getContext();
        String creator = context.getString(R.string.gallery_by_creator, item.creatorName);
        String category = context.getString(TemplateUi.categoryLabel(item.category));
        String categoryAndDownloads = context.getString(R.string.template_status_with_count, category,
                context.getString(R.string.gallery_downloads, TemplateUi.count(item.downloads)));
        if (holder.binding instanceof ItemGalleryCardBinding) {
            ItemGalleryCardBinding b = (ItemGalleryCardBinding) holder.binding;
            b.name.setText(item.name);
            b.creator.setText(creator);
            b.category.setText(category);
        } else {
            ItemGalleryTemplateBinding b = (ItemGalleryTemplateBinding) holder.binding;
            b.name.setText(item.name);
            b.creator.setText(creator);
            b.category.setText(categoryAndDownloads);
        }
        holder.binding.getRoot().setOnClickListener(v -> onClick.accept(item));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ViewBinding binding;

        Holder(ViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static final DiffUtil.ItemCallback<GalleryTemplateDto> DIFF = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull GalleryTemplateDto a, @NonNull GalleryTemplateDto b) {
            return Objects.equals(a.id, b.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull GalleryTemplateDto a, @NonNull GalleryTemplateDto b) {
            return Objects.equals(a.name, b.name) && Objects.equals(a.creatorName, b.creatorName)
                    && Objects.equals(a.category, b.category) && a.downloads == b.downloads;
        }
    };
}
