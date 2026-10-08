package com.aris.templateapp.ui.creator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.aris.templateapp.data.local.ProjectEntity;
import com.aris.templateapp.databinding.ItemProjectBinding;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Kartu project di tab Project. Klik kartu = buka editor; tombol ⋮ = menu aksi. */
public class ProjectAdapter extends ListAdapter<ProjectEntity, ProjectAdapter.Holder> {

    private final Consumer<ProjectEntity> onClick;
    private final BiConsumer<View, ProjectEntity> onMenu;

    public ProjectAdapter(Consumer<ProjectEntity> onClick, BiConsumer<View, ProjectEntity> onMenu) {
        super(DIFF);
        this.onClick = onClick;
        this.onMenu = onMenu;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemProjectBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ProjectEntity project = getItem(position);
        ItemProjectBinding b = holder.binding;
        b.name.setText(project.name);
        ProjectUi.bindStatusBadge(b.statusBadge, project.status);
        ProjectThumbnails.load(b.thumbnail, project.thumbnailPath);
        b.mode.setText(ProjectUi.modeLabel(project.mode));
        b.edited.setText(ProjectUi.timeLine(b.getRoot().getContext(), project));
        ProjectUi.bindInfo(b.info, project);
        b.getRoot().setOnClickListener(v -> onClick.accept(project));
        b.menuButton.setOnClickListener(v -> onMenu.accept(v, project));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemProjectBinding binding;

        Holder(ItemProjectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static final DiffUtil.ItemCallback<ProjectEntity> DIFF = new DiffUtil.ItemCallback<>() {
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
}
