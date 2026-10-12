package com.aris.templateapp.ui.workspace;

import androidx.annotation.StringRes;

import com.aris.templateapp.R;
import com.aris.templateapp.data.model.WorkspaceRole;

/** Label tampilan untuk data ruang kerja. */
public final class WorkspaceUi {

    private WorkspaceUi() {
    }

    @StringRes
    public static int roleLabel(WorkspaceRole role) {
        switch (role) {
            case OWNER:
                return R.string.role_owner;
            case ADMIN:
                return R.string.role_admin;
            case EDITOR:
            default:
                return R.string.role_editor;
        }
    }
}
