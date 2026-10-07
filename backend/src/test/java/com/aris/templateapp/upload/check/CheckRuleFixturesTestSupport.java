package com.aris.templateapp.upload.check;

import com.aris.templateapp.config.AppProperties;

/** Pengaturan upload untuk test di package lain (sama dengan application.yml, batas ukuran diperkecil). */
public final class CheckRuleFixturesTestSupport {

    private CheckRuleFixturesTestSupport() {
    }

    public static AppProperties.Upload settings() {
        return CheckRuleFixturesTest.testSettings();
    }
}
