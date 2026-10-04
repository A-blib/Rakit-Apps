package com.aris.templateapp.data.model;

import androidx.annotation.Nullable;

import java.util.Objects;

/** Filter tab Project (alur-pembuatan-website.md bagian 4.1): status, kata kunci nama, dan urutan. Tidak bisa diubah. */
public final class ProjectFilter {

    public enum Sort { UPDATED, CREATED, NAME }

    public static final ProjectFilter DEFAULT = new ProjectFilter(null, "", Sort.UPDATED);

    /** null = semua status. */
    @Nullable
    public final ProjectStatus status;
    public final String query;
    public final Sort sort;

    public ProjectFilter(@Nullable ProjectStatus status, String query, Sort sort) {
        this.status = status;
        this.query = query == null ? "" : query.trim();
        this.sort = sort;
    }

    public ProjectFilter withStatus(@Nullable ProjectStatus status) {
        return new ProjectFilter(status, query, sort);
    }

    public ProjectFilter withQuery(String query) {
        return new ProjectFilter(status, query, sort);
    }

    public ProjectFilter withSort(Sort sort) {
        return new ProjectFilter(status, query, sort);
    }

    /** "Hapus filter": semua status, tanpa pencarian; urutan tetap. */
    public ProjectFilter cleared() {
        return new ProjectFilter(null, "", sort);
    }

    public boolean isFiltered() {
        return status != null || !query.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProjectFilter)) {
            return false;
        }
        ProjectFilter other = (ProjectFilter) o;
        return status == other.status && query.equals(other.query) && sort == other.sort;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, query, sort);
    }
}
