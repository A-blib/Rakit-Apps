package com.aris.templateapp.ui.creator;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.aris.templateapp.data.local.ProjectEntity;
import com.aris.templateapp.databinding.ItemProjectSmallBinding;

import java.util.Objects;
import java.util.function.Consumer;

/** Kartu kecil "Lanjutkan project" di Beranda yang bisa digeser ke samping. */
public class ProjectSmallAdapter extends ListAdapter<ProjectEntity, ProjectSmallAdapter.Holder> {

    private static final DiffUtil.ItemCallback<ProjectEntity> DIFF = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull ProjectEntity a, @NonNull ProjectEntity b) {
            return a.id.equals(b.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull ProjectEntity a, @NonNull ProjectEntity b) {
            return a.name.equals(b.name) && a.status.equals(b.status) && a.mode.equals(b.mode)
                    && a.updatedAt == b.updatedAt && a.missingCount == b.missingCount
                    && Objects.equals(a.lastExportedAt, b.lastExportedAt);
        }
    };

    private final Consumer<ProjectEntity> onClick;

    public ProjectSmallAdapter(Consumer<ProjectEntity> onClick) {
        super(DIFF);
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemProjectSmallBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ProjectEntity project = getItem(position);
        holder.binding.name.setText(project.name);
        ProjectThumbnails.load(holder.binding.thumbnail, project.thumbnailPath);
        holder.binding.status.setText(ProjectUi.compactStatus(holder.binding.getRoot().getContext(), project));
        holder.binding.getRoot().setOnClickListener(v -> onClick.accept(project));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemProjectSmallBinding binding;

        Holder(ItemProjectSmallBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
