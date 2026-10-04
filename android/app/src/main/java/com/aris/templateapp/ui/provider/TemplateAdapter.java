package com.aris.templateapp.ui.provider;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.aris.templateapp.R;
import com.aris.templateapp.data.remote.dto.TemplateListDto.TemplateSummaryDto;
import com.aris.templateapp.databinding.ItemTemplateBinding;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Kartu "Template Anda". {@link ListAdapter} membandingkan daftar lama & baru (DiffUtil) sehingga saat halaman
 * berikutnya dimuat hanya kartu baru yang digambar, bukan seluruh daftar.
 */
public class TemplateAdapter extends ListAdapter<TemplateSummaryDto, TemplateAdapter.Holder> {

    private final Consumer<TemplateSummaryDto> onClick;

    public TemplateAdapter(Consumer<TemplateSummaryDto> onClick) {
        super(DIFF);
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemTemplateBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        TemplateSummaryDto item = getItem(position);
        ItemTemplateBinding b = holder.binding;
        Context context = b.getRoot().getContext();
        b.name.setText(item.name);
        b.status.setText(TemplateUi.statusLine(context, item.status, item.errorCount, item.warningCount));
        b.status.setTextColor(ContextCompat.getColor(context, statusColor(item)));

        String category = context.getString(TemplateUi.categoryLabel(item.category));
        String updated = TemplateUi.updatedAgo(context, item.updatedAt);
        // Angka dilihat/download hanya bermakna untuk template yang pernah tayang.
        boolean hasStats = TemplateUi.PUBLISHED.equals(item.status) || TemplateUi.DISABLED.equals(item.status);
        b.meta.setText(hasStats
                ? context.getString(R.string.template_card_meta, category, item.views, item.downloads, updated)
                : context.getString(R.string.template_card_meta_short, category, updated));
        b.getRoot().setOnClickListener(v -> onClick.accept(item));
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
        final ItemTemplateBinding binding;

        Holder(ItemTemplateBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
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
                    && a.views == b.views && a.downloads == b.downloads;
        }
    };
}
