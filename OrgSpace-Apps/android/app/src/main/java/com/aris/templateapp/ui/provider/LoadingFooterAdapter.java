package com.aris.templateapp.ui.provider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aris.templateapp.R;

/** Satu baris indikator loading di bawah daftar saat halaman berikutnya dimuat (digabung lewat ConcatAdapter). */
public class LoadingFooterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private boolean loading;

    public void setLoading(boolean loading) {
        if (this.loading == loading) {
            return;
        }
        this.loading = loading;
        if (loading) {
            notifyItemInserted(0);
        } else {
            notifyItemRemoved(0);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading_footer, parent, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Tidak ada data yang perlu dipasang.
    }

    @Override
    public int getItemCount() {
        return loading ? 1 : 0;
    }
}
