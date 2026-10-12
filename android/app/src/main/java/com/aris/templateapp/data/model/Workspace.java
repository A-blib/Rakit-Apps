package com.aris.templateapp.data.model;

import androidx.annotation.Nullable;

import java.util.Objects;

/** Ruang kerja (organisasi atau ruang pribadi) beserta peran user di dalamnya. */
public class Workspace {

    private final String id;
    private final boolean personal;
    private final String name;
    @Nullable private final String shortName;
    @Nullable private final String logoUrl;
    private final WorkspaceRole role;
    /** Kode kategori & jenjang organisasi; null untuk ruang pribadi (atau data lama sebelum W4). */
    @Nullable private final String category;
    @Nullable private final String level;

    public Workspace(String id, boolean personal, String name, @Nullable String shortName, @Nullable String logoUrl,
                     WorkspaceRole role, @Nullable String category, @Nullable String level) {
        this.id = id;
        this.personal = personal;
        this.name = name;
        this.shortName = shortName;
        this.logoUrl = logoUrl;
        this.role = role;
        this.category = category;
        this.level = level;
    }

    @Nullable
    public String getCategory() {
        return category;
    }

    @Nullable
    public String getLevel() {
        return level;
    }

    public String getId() {
        return id;
    }

    public boolean isPersonal() {
        return personal;
    }

    public String getName() {
        return name;
    }

    /** Nama untuk kalimat sapaan ({org}): nama singkat jika ada (revisi-fondasi-orgspace.md 8.1). */
    public String getGreetingName() {
        return shortName != null && !shortName.isEmpty() ? shortName : name;
    }

    @Nullable
    public String getLogoUrl() {
        return logoUrl;
    }

    public WorkspaceRole getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Workspace)) {
            return false;
        }
        Workspace other = (Workspace) o;
        return personal == other.personal && id.equals(other.id) && name.equals(other.name)
                && Objects.equals(shortName, other.shortName) && Objects.equals(logoUrl, other.logoUrl)
                && role == other.role && Objects.equals(category, other.category) && Objects.equals(level, other.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, personal, name, shortName, logoUrl, role, category, level);
    }
}
