package com.aris.templateapp.ui.website;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.aris.templateapp.data.model.Website;

/** Argumen navigasi halaman kelola website: ID website + ruang kerja pemiliknya + nama (judul sementara). */
public final class WebsiteArgs {

    static final String WEBSITE_ID = "websiteId";
    static final String ORGANIZATION_ID = "organizationId";
    static final String NAME = "name";

    private WebsiteArgs() {
    }

    public static Bundle of(Website website) {
        return of(website.id, website.organizationId, website.name);
    }

    public static Bundle of(String websiteId, String organizationId, String name) {
        Bundle args = new Bundle();
        args.putString(WEBSITE_ID, websiteId);
        args.putString(ORGANIZATION_ID, organizationId);
        args.putString(NAME, name);
        return args;
    }

    static String websiteId(Fragment fragment) {
        return fragment.requireArguments().getString(WEBSITE_ID);
    }

    static String organizationId(Fragment fragment) {
        return fragment.requireArguments().getString(ORGANIZATION_ID);
    }

    static String name(Fragment fragment) {
        return fragment.requireArguments().getString(NAME);
    }
}
