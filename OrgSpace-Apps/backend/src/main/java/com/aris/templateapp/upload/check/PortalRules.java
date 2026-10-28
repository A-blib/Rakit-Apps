package com.aris.templateapp.upload.check;

import com.aris.templateapp.site.PortalConfig;
import com.aris.templateapp.site.PublicSiteController;

import java.util.List;

/** portal.json opsional di folder paling luar: dibaca dengan aturan yang sama seperti saat Portal dibuka. */
final class PortalRules {

    // Cukup beberapa pesan pertama; sisanya biasanya akibat salah yang sama.
    private static final int MAX_REPORTED = 10;

    private final CheckContext ctx;

    PortalRules(CheckContext ctx) {
        this.ctx = ctx;
    }

    void check() {
        String json = ctx.text(PortalConfig.FILE_NAME);
        if (json == null) {
            return;
        }
        StringBuilder html = new StringBuilder();
        for (String page : ctx.files.pages()) {
            String text = ctx.text(page);
            html.append(text == null ? "" : text);
        }
        List<String> errors = PortalConfig.read(json, PublicSiteController.templateModules(html.toString()),
                ctx.files::exists).errors();
        for (String error : errors.subList(0, Math.min(MAX_REPORTED, errors.size()))) {
            ctx.findings.add(CheckRule.PORTAL_CONFIG_INVALID, error, PortalConfig.FILE_NAME, null,
                    "Perbaiki portal.json sesuai panduan Mengatur Tampilan Portal Pengurus, atau hapus file ini untuk memakai tampilan standar.");
        }
    }
}
