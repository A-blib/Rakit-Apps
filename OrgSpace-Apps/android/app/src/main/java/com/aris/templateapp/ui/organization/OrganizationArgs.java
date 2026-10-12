package com.aris.templateapp.ui.organization;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

/** Argumen navigasi bersama layar kelola organisasi: ID ruang kerja yang dikelola. */
public final class OrganizationArgs {

    public static final String ORGANIZATION_ID = "organizationId";

    private OrganizationArgs() {
    }

    public static Bundle of(String organizationId) {
        Bundle args = new Bundle();
        args.putString(ORGANIZATION_ID, organizationId);
        return args;
    }

    public static String id(Fragment fragment) {
        return fragment.requireArguments().getString(ORGANIZATION_ID);
    }
}
