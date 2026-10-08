package com.aris.templateapp.ui.creator;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.aris.templateapp.data.local.ProjectEntity;
import com.aris.templateapp.databinding.ItemProjectSmallBinding;

import java.util.function.Consumer;

/** Kartu kecil "Lanjutkan project" di Beranda yang bisa digeser ke samping. */
public class ProjectSmallAdapter extends ListAdapter<ProjectEntity, ProjectSmallAdapter.Holder> {

    private final Consumer<ProjectEntity> onClick;

    public ProjectSmallAdapter(Consumer<ProjectEntity> onClick) {
        super(ProjectAdapter.DIFF);
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
