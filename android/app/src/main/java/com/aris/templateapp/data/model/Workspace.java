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

    public Workspace(String id, boolean personal, String name, @Nullable String shortName, @Nullable String logoUrl,
                     WorkspaceRole role) {
        this.id = id;
        this.personal = personal;
        this.name = name;
        this.shortName = shortName;
        this.logoUrl = logoUrl;
        this.role = role;
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
                && role == other.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, personal, name, shortName, logoUrl, role);
    }
}
