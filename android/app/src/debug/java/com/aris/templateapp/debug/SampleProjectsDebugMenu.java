package com.aris.templateapp.debug;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.aris.templateapp.R;
import com.aris.templateapp.data.repository.ProjectRepository;
import com.aris.templateapp.ui.settings.DebugMenu;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

/** Grup DEBUG di Pengaturan: "Isi project contoh" dan "Hapus project contoh" (alur-pembuatan-website.md 6.1). */
public class SampleProjectsDebugMenu implements DebugMenu {

    private final ProjectRepository repository;

    @Inject
    public SampleProjectsDebugMenu(ProjectRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addTo(ViewGroup container, Fragment host) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        TextView title = new TextView(container.getContext(), null, 0, R.style.Widget_App_ListGroupTitle);
        title.setText(R.string.debug_group);
        container.addView(title);

        TextView fill = row(inflater, container, R.string.debug_fill_projects, R.drawable.ic_add);
        fill.setOnClickListener(v -> {
            var samples = SampleProjects.create(System.currentTimeMillis());
            repository.insertAll(samples);
            Snackbar.make(v, host.getString(R.string.debug_projects_added, samples.size()), Snackbar.LENGTH_SHORT).show();
        });
        TextView clear = row(inflater, container, R.string.debug_clear_projects, R.drawable.ic_block);
        clear.setOnClickListener(v -> repository.deleteSamples(count ->
                Snackbar.make(v, host.getString(R.string.debug_projects_removed, count), Snackbar.LENGTH_SHORT).show()));
    }

    private static TextView row(LayoutInflater inflater, ViewGroup container, int text, int icon) {
        TextView row = (TextView) inflater.inflate(R.layout.item_debug_row, container, false);
        row.setText(text);
        row.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0);
        container.addView(row);
        return row;
    }
}
