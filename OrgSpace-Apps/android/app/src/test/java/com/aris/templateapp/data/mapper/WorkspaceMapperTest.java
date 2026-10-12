package com.aris.templateapp.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.aris.templateapp.data.model.OrgCategories;
import com.aris.templateapp.data.model.Workspace;
import com.aris.templateapp.data.model.WorkspaceRole;
import com.aris.templateapp.data.remote.dto.CategoriesDto;
import com.aris.templateapp.data.remote.dto.OrganizationSummaryDto;

import org.junit.Test;

import java.util.List;

public class WorkspaceMapperTest {

    @Test
    public void summaryToWorkspace() {
        OrganizationSummaryDto dto = new OrganizationSummaryDto();
        dto.id = "org-1";
        dto.type = "personal";
        dto.name = "Ruang Aris";
        dto.myRole = "owner";

        Workspace workspace = WorkspaceMapper.toModel(dto);

        assertTrue(workspace.isPersonal());
        assertEquals(WorkspaceRole.OWNER, workspace.getRole());
        assertEquals("Ruang Aris", workspace.getGreetingName());
    }

    @Test
    public void greetingPrefersShortName() {
        OrganizationSummaryDto dto = new OrganizationSummaryDto();
        dto.id = "org-2";
        dto.type = "organization";
        dto.name = "OSIS SMA Negeri 1 Bandung";
        dto.shortName = "OSIS Smansa";
        dto.myRole = "something-new";

        Workspace workspace = WorkspaceMapper.toModel(dto);

        assertFalse(workspace.isPersonal());
        assertEquals("OSIS Smansa", workspace.getGreetingName());
        // Peran yang belum dikenal app dianggap peran dengan hak paling sedikit.
        assertEquals(WorkspaceRole.EDITOR, workspace.getRole());
    }

    @Test
    public void categoriesFilteredByLevel() {
        CategoriesDto dto = new CategoriesDto();
        CategoriesDto.Category osis = new CategoriesDto.Category();
        osis.value = "osis_mpk";
        osis.label = "OSIS / MPK";
        osis.levels = List.of("smp", "sma", "smk");
        CategoriesDto.Category bem = new CategoriesDto.Category();
        bem.value = "bem_dema_dpm";
        bem.label = "BEM";
        bem.levels = List.of("perguruan_tinggi");
        dto.categories = List.of(osis, bem);
        dto.levels = List.of();

        OrgCategories categories = WorkspaceMapper.toModel(dto);

        assertEquals(1, categories.categoriesFor("sma").size());
        assertEquals("osis_mpk", categories.categoriesFor("sma").get(0).value);
        assertEquals("bem_dema_dpm", categories.categoriesFor("perguruan_tinggi").get(0).value);
        assertTrue(categories.categoriesFor(null).isEmpty());
    }

    @Test
    public void roleOrder() {
        assertTrue(WorkspaceRole.OWNER.atLeast(WorkspaceRole.ADMIN));
        assertTrue(WorkspaceRole.ADMIN.atLeast(WorkspaceRole.ADMIN));
        assertFalse(WorkspaceRole.EDITOR.atLeast(WorkspaceRole.ADMIN));
    }
}
