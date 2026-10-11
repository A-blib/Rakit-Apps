package com.aris.templateapp.data.mapper;

import com.aris.templateapp.data.model.InvitationPreview;
import com.aris.templateapp.data.model.OrgCategories;
import com.aris.templateapp.data.model.Workspace;
import com.aris.templateapp.data.model.WorkspaceRole;
import com.aris.templateapp.data.remote.dto.CategoriesDto;
import com.aris.templateapp.data.remote.dto.InvitationPreviewDto;
import com.aris.templateapp.data.remote.dto.OrganizationDto;
import com.aris.templateapp.data.remote.dto.OrganizationSummaryDto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/** DTO organisasi → model UI. */
public final class WorkspaceMapper {

    static final String TYPE_PERSONAL = "personal";

    private WorkspaceMapper() {
    }

    public static Workspace toModel(OrganizationSummaryDto dto) {
        return new Workspace(dto.id, TYPE_PERSONAL.equals(dto.type), dto.name, dto.shortName, dto.logoUrl,
                WorkspaceRole.fromValue(dto.myRole));
    }

    /** Ringkasan dari profil lengkap, mis. setelah membuat atau bergabung organisasi. */
    public static OrganizationSummaryDto toSummary(OrganizationDto dto) {
        OrganizationSummaryDto summary = new OrganizationSummaryDto();
        summary.id = dto.id;
        summary.type = dto.type;
        summary.name = dto.name;
        summary.shortName = dto.shortName;
        summary.logoUrl = dto.logoUrl;
        summary.myRole = dto.myRole;
        return summary;
    }

    public static List<Workspace> toModels(List<OrganizationSummaryDto> dtos) {
        List<Workspace> result = new ArrayList<>();
        if (dtos != null) {
            for (OrganizationSummaryDto dto : dtos) {
                result.add(toModel(dto));
            }
        }
        return result;
    }

    public static InvitationPreview toModel(InvitationPreviewDto dto) {
        return new InvitationPreview(dto.organizationName, dto.logoUrl, WorkspaceRole.fromValue(dto.role));
    }

    public static OrgCategories toModel(CategoriesDto dto) {
        List<OrgCategories.Option> levels = new ArrayList<>();
        if (dto.levels != null) {
            for (CategoriesDto.Level level : dto.levels) {
                levels.add(new OrgCategories.Option(level.value, level.label));
            }
        }
        List<OrgCategories.Category> categories = new ArrayList<>();
        if (dto.categories != null) {
            for (CategoriesDto.Category category : dto.categories) {
                categories.add(new OrgCategories.Category(category.value, category.label,
                        new HashSet<>(category.levels == null ? List.of() : category.levels)));
            }
        }
        return new OrgCategories(levels, categories);
    }
}
