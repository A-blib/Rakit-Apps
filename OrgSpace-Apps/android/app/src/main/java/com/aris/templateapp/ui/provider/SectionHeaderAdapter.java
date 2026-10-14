package com.aris.templateapp.ui.provider;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import com.aris.templateapp.R;

/** Satu judul kelompok (mis. "FOLDER") di dalam daftar, digabung lewat ConcatAdapter; bisa disembunyikan. */
public class SectionHeaderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @StringRes
    private final int title;
    private boolean visible;

    public SectionHeaderAdapter(@StringRes int title) {
        this.title = title;
    }

    public void setVisible(boolean visible) {
        if (this.visible == visible) {
            return;
        }
        this.visible = visible;
        if (visible) {
            notifyItemInserted(0);
        } else {
            notifyItemRemoved(0);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView view = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_section_header, parent, false);
        view.setText(title);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Teks sudah dipasang saat view dibuat.
    }

    @Override
    public int getItemCount() {
        return visible ? 1 : 0;
    }
}
